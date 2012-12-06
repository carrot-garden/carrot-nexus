/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskInterruptedException;
import org.sonatype.scheduling.TaskState;
import org.sonatype.sisu.resource.scanner.Listener;
import org.sonatype.sisu.resource.scanner.Scanner;
import org.sonatype.sisu.resource.scanner.helper.ListenerSupport;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttribute;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigCapability;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;
import com.carrotgarden.nexus.aws.s3.publish.metrics.Reporter;
import com.carrotgarden.nexus.aws.s3.publish.metrics.TaskReporter;
import com.carrotgarden.nexus.aws.s3.publish.util.AmazonHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.PathHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.RepoHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.TaskHelp;
import com.google.common.base.Throwables;
import com.yammer.metrics.core.Gauge;

/**
 */
@Named(ScannerTask.NAME)
public class ScannerTask extends AbstractNexusTask<Object> {

	public enum ConfigType {
		/** task must run on configuration change */
		ON_DEMAND, //
		/** task must run on schedule, re-set after configuration change */
		SCHEDULED, //
	}

	private static final String KEY_CONFIG_ID = "scanner.task.config-id";
	private static final String KEY_CONFIG_TYPE = "scanner.task.config-type";
	private static final String KEY_NAME = "scanner.task.name";

	public static final String NAME = "ScannerTask";

	/** nexus task name/type convention */
	static {
		final Named anno = ScannerTask.class.getAnnotation(Named.class);
		if (anno == null) {
			throw new IllegalStateException("@Named is missing");
		}
		if (!anno.value().equals(ScannerTask.class.getSimpleName())) {
			throw new IllegalStateException("@Named must match class name");
		}
	}

	public static String taskNameRule(final String configId,
			final ConfigType configType) {
		return NAME + " [" + configId + "] " + configType + " ("
				+ ConfigHelp.reference().getString("plugin-name") + ")";
	}

	private final CapabilityRegistry capaRegistry;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final RepositoryRegistry repoRegistry;

	private final Scanner scanner;

	private final TaskReporter reporter;

	private NexusScheduler taskScheduler;

	@Inject
	public ScannerTask( //
			final TaskReporter reporter, //
			final NexusScheduler taskScheduler, //
			@Named("serial") final Scanner scanner, //
			final CapabilityRegistry capaRegistry, //
			final RepositoryRegistry repoRegistry //
	) {

		this.taskScheduler = taskScheduler;
		this.reporter = reporter;
		this.scanner = scanner;
		this.capaRegistry = capaRegistry;
		this.repoRegistry = repoRegistry;

		reporter.registry().newGauge(getClass(), "current task state",
				new Gauge<String>() {
					@Override
					public String value() {
						return taskState();
					}
				});

	}

	private String taskState() {
		try {
			final ScheduledTask<?> reference = //
			TaskHelp.reference(taskScheduler, this);
			final TaskState state = reference.getTaskState();
			return state.name();
		} catch (final Exception e) {
			return "UNKNOWN";
		}
	}

	public String configId() {
		return getParameters().get(KEY_CONFIG_ID);
	}

	public void configId(final String configId) {
		getParameters().put(KEY_CONFIG_ID, configId);
	}

	public ConfigType configType() {
		return ConfigType.valueOf(getParameters().get(KEY_CONFIG_TYPE));
	}

	public void configType(final ConfigType type) {
		getParameters().put(KEY_CONFIG_TYPE, type.name());
	}

	@Override
	protected Object doRun() {
		reporter.taskRunCount.inc();
		try {
			doWork();
		} catch (final InterruptedException e) {
			log.debug("task cancel 1");
		} catch (final TaskInterruptedException e) {
			log.debug("task cancel 2");
		} catch (final Exception e) {
			log.error("task failed", e);
		}
		return null;
	}

	private void doSleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
			throw new TaskInterruptedException("sleep interrupted", e);
		} finally {
		}
	}

	private void doWork() throws Exception {

		final CapabilityIdentity capaId = new CapabilityIdentity(configId());

		final CapabilityReference reference = capaRegistry.get(capaId);

		final ConfigEntry entry = reference
				.capabilityAs(ConfigCapability.class);

		final String comboId = entry.comboId();

		final AmazonService amazonService = entry.amazonService();

		final List<String> repoList = RepoHelp.repoList(repoRegistry, comboId);

		for (final String repoId : repoList) {

			checkInterruption();

			doSleep(scannerRepositorySleepTime());

			final Repository repo = repoRegistry.getRepository(repoId);

			final File root = RepoHelp.repoRoot(repo);

			final Listener listener = new ListenerSupport() {

				@Override
				public void onBegin() {

					log.info("##########################################");
					log.info("repo scan init : {} {}", configId(), repoId);

				}

				@Override
				public void onEnd() {

					log.info("repo stats : total={} success={}",
							reporter.amazonPublishedFileSize.count(),
							reporter.amazonPublishedFileCount.count());
					log.info("repo scan done : {} {}", configId(), repoId);
					log.info("##########################################");
				}

				@Override
				public void onFile(final File file) {
					try {

						reporter.repoFilePeek.add(file);

						reporter.fileRate.mark();
						reporter.fileCount.inc();

						checkInterruption();

						final String path = //
						PathHelp.rootFullPath(PathHelp.relativePath(root, file));

						if (entry.isExcluded(path)) {
							reporter.amazonIgnoredFileCount.inc();
							return;
						}

						final ResourceStoreRequest request = //
						new ResourceStoreRequest(path);

						request.getRequestContext().put(//
								AccessManager.REQUEST_AUTHORIZED, "true");

						final StorageItem any = repo.retrieveItem(request);

						final boolean isFile = any instanceof StorageFileItem;

						if (!isFile) {
							return;
						}

						final StorageFileItem item = (StorageFileItem) any;

						final Attributes attributes = item
								.getRepositoryItemAttributes();

						final String value = attributes
								.get(CarrotAttribute.ATTR_IS_SAVED);

						if ("true".equals(value)) {
							return;
						}

						int countSleep = 0;

						while (true) {

							final boolean isSaved = AmazonHelp.storeItem(
									amazonService, repo, item, file, log);

							if (isSaved) {
								reporter.amazonPublishedFileCount.inc();
								reporter.amazonPublishedFileSize.inc(file
										.length());
								break;
							} else {
								reporter.amazonRetriedFileCount.inc();
							}

							if (countSleep == 0) {
								log.warn("amazon failure;  will wait and try again");
							}

							doSleep(scannerFailureSleepTime());

							countSleep++;

						}

					} catch (final Exception e) {
						Throwables.propagate(e);
					}
				}

			};

			scanner.scan(root, listener);

			final String report = reporter.report();

			log.info("\n{}", report);

		}

	}

	public boolean equals(final String configId, final ConfigType type) {
		return configId().equals(configId) && configType().equals(type);
	}

	@Override
	protected String getAction() {
		return "scanner";
	}

	@Override
	protected String getMessage() {
		return name();
	}

	public String name() {
		return getParameters().get(KEY_NAME);
	}

	public void name(final String name) {
		getParameters().put(KEY_NAME, name);
	}

	public long scannerFailureSleepTime() {
		return ConfigHelp.reference().getMilliseconds(
				"scanner-task.failure-sleep-time");
	}

	public long scannerRepositorySleepTime() {
		return ConfigHelp.reference().getMilliseconds(
				"scanner-task.repository-sleep-time");
	}

	public Reporter reporter() {
		return reporter;
	}

}
