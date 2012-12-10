/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import static com.carrotgarden.nexus.aws.s3.publish.util.PathHelp.*;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

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
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskInterruptedException;
import org.sonatype.scheduling.TaskState;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttribute;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigCapability;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;
import com.carrotgarden.nexus.aws.s3.publish.mailer.CarrotMailer;
import com.carrotgarden.nexus.aws.s3.publish.mailer.Report;
import com.carrotgarden.nexus.aws.s3.publish.metrics.Reporter;
import com.carrotgarden.nexus.aws.s3.publish.metrics.TaskReporter;
import com.carrotgarden.nexus.aws.s3.publish.scanner.CarrotListener;
import com.carrotgarden.nexus.aws.s3.publish.scanner.CarrotListenerSupport;
import com.carrotgarden.nexus.aws.s3.publish.scanner.CarrotScanner;
import com.carrotgarden.nexus.aws.s3.publish.util.AmazonHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.RepoHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.TaskHelp;
import com.google.common.base.Throwables;
import com.yammer.metrics.core.Gauge;

/**
 * scan repository and publish newly found files to amazon
 */
@Named(ScannerTask.NAME)
public class ScannerTask extends BaseTask {

	public enum ConfigType {

		/** a task must run on a configuration change */
		ON_DEMAND, //

		/** a task must run on provided schedule */
		SCHEDULED, //

	}

	/** persistent task binding to owner capability */
	private static final String KEY_CONFIG_ID = "scanner.task.config-id";
	private static final String KEY_CONFIG_TYPE = "scanner.task.config-type";

	public static final String NAME = "ScannerTask";

	public static String taskNameRule(final String configId,
			final ConfigType configType) {
		return NAME + " [" + configId + "] " + configType + " ("
				+ ConfigHelp.pluginName() + ")";
	}

	private final CapabilityRegistry capaRegistry;
	private final RepositoryRegistry repoRegistry;
	private final CarrotScanner scanner;
	private final TaskReporter reporter;
	private final NexusScheduler scheduler;
	private final CarrotMailer mailer;

	@Inject
	public ScannerTask( //
			final CarrotMailer mailer, //
			final TaskReporter reporter, //
			final NexusScheduler scheduler, //
			@Named("serial") final CarrotScanner scanner, //
			final CapabilityRegistry capaRegistry, //
			final RepositoryRegistry repoRegistry //
	) {
		this.mailer = mailer;
		this.scheduler = scheduler;
		this.reporter = reporter;
		this.scanner = scanner;
		this.capaRegistry = capaRegistry;
		this.repoRegistry = repoRegistry;

		reporter.newGauge("task name", new Gauge<String>() {
			@Override
			public String value() {
				return getName();
			}
		});
		reporter.newGauge("task state", new Gauge<String>() {
			@Override
			public String value() {
				return state();
			}
		});
		reporter.newGauge("task failure", new Gauge<String>() {
			@Override
			public String value() {
				return "" + failure;
			}
		});

	}

	private String state() {
		try {
			final ScheduledTask<?> reference = //
			TaskHelp.reference(scheduler, this);
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

	private Throwable failure;

	public Throwable failure() {
		return failure;
	}

	@Override
	protected Object doRun() {

		reporter.taskRunCount.inc();

		boolean isSuccess = false;

		try {
			failure = null;
			doWork();
			isSuccess = true;
			log.info("task success");
		} catch (final InterruptedException e) {
			failure = e;
			log.debug("task cancel 1");
		} catch (final TaskInterruptedException e) {
			failure = e;
			log.debug("task cancel 2");
		} catch (final Exception e) {
			failure = e;
			log.error("task failure", e);
		}

		log.info("\n{}", reporter.report());

		if (isSuccess) {
			mailer.sendScannerReport(Report.SCANNER_TASK_SUCCESS,
					configEntry(), this);
			mailer.sendScannerReport(Report.SCANNER_TASK_SUCCESS_REPORT,
					configEntry(), this);
		} else {
			mailer.sendScannerReport(Report.SCANNER_TASK_FAILURE,
					configEntry(), this);
			mailer.sendScannerReport(Report.SCANNER_TASK_FAILURE_REPORT,
					configEntry(), this);
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

	private ConfigEntry configEntry() {

		final CapabilityIdentity capaId = new CapabilityIdentity(configId());

		final CapabilityReference reference = capaRegistry.get(capaId);

		final ConfigEntry entry = reference
				.capabilityAs(ConfigCapability.class);

		return entry;

	}

	private void doWork() throws Exception {

		reporter.reset();

		if (shouldYield()) {
			log.info("yielding to priority tasks");
			return;
		}

		final ConfigEntry entry = configEntry();

		final String comboId = entry.comboId();

		final AmazonService service = entry.amazonService();

		final List<String> repoList = RepoHelp.repoList(repoRegistry, comboId);

		final Pattern defaultExclude = ConfigHelp.defaultExclude();

		for (final String repoId : repoList) {

			checkInterruption();

			doSleep(scannerRepositorySleepTime());

			final Repository repo = repoRegistry.getRepository(repoId);

			final File root = RepoHelp.repoRoot(repo);

			final CarrotListener listener = new CarrotListenerSupport() {

				@Override
				public void onBegin() {

					log.info("##########################################");
					log.info("repo scan init : {} {}", configId(), repoId);

				}

				@Override
				public void onEnd() {

					log.info("repo stats : processed={} published={}",
							reporter.scanCount.count(),
							reporter.amazonPublishedFileCount.count());
					log.info("repo scan done : {} {}", configId(), repoId);
					log.info("##########################################");
				}

				@Override
				public boolean skipDirectory(final File directory) {

					final String path = //
					rootFullPath(relativePath(root, directory));

					final boolean isDefaultExclude = //
					defaultExclude.matcher(path).matches();

					if (isDefaultExclude) {
						log.info("ignore direcotry : {}", directory);
						reporter.skipFileWatch.add(directory
								+ "[default exclude]");
					}

					return isDefaultExclude;

				}

				@Override
				public boolean skipFile(final File file) {

					return false;

				}

				@Override
				public void onFile(final File file) {
					try {

						checkInterruption();

						reporter.scanCount.inc();
						reporter.scanRate.mark();
						reporter.repoFileWatch.add(file);

						final String path = //
						rootFullPath(relativePath(root, file));

						if (entry.isExcluded(path)) {
							reporter.amazonIgnoredFileCount.inc();
							reporter.skipFileWatch.add(file
									+ "[entry is excluded]");
							return;
						}

						final ResourceStoreRequest request = //
						new ResourceStoreRequest(path);

						request.getRequestContext().put(//
								AccessManager.REQUEST_AUTHORIZED, "true");

						final StorageItem any = repo.retrieveItem(request);

						final boolean isFileItem = any instanceof StorageFileItem;

						if (!isFileItem) {
							reporter.amazonIgnoredFileCount.inc();
							reporter.skipFileWatch.add(file
									+ "[not a file item]");
							return;
						}

						reporter.repoFileCount.inc();
						reporter.repoFileSize.inc(file.length());

						final StorageFileItem item = (StorageFileItem) any;

						final Attributes attributes = item
								.getRepositoryItemAttributes();

						final String value = attributes
								.get(CarrotAttribute.ATTR_IS_SAVED);

						if ("true".equals(value)) {
							reporter.amazonExistingFileCount.inc();
							reporter.skipFileWatch.add(file
									+ "[already stored]");
							return;
						}

						int countSleep = 0;

						/** block till bucket available */

						while (true) {

							checkInterruption();

							final boolean isAmazonSaved = AmazonHelp.storeItem(
									service, repo, item, file, log);

							if (isAmazonSaved) {
								reporter.saveFileWatch.add(file);
								reporter.amazonPublishedFileCount.inc();
								reporter.amazonPublishedFileSize.inc( //
										file.length());
								break;
							} else {
								reporter.amazonRetriedFileCount.inc();
								reporter.skipFileWatch.add(file
										+ "[amazon failure]");
							}

							if (countSleep == 0) {
								log.warn(
										"amazon failure - will wait and try again : {}",
										file);
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

		}

	}

	/** scheduled tasks should yield to on-demand tasks, if any */
	private boolean shouldYield() {

		/** self is on-demand */
		if (configType() == ConfigType.ON_DEMAND) {
			return false;
		}

		/** no scanner tasks */
		final List<ScheduledTask<?>> referenceList = //
		scheduler.getAllTasks().get(NAME);

		if (referenceList == null) {
			return false;
		}

		/** any other on-demand scanner task running ? */
		for (final ScheduledTask<?> reference : referenceList) {

			final ScannerTask task = (ScannerTask) reference.getTask();

			if (task.configType() == ConfigType.ON_DEMAND) {
				return true;
			}

		}

		return false;

	}

	public boolean equals(final String configId, final ConfigType type) {
		return configId().equals(configId) && configType().equals(type);
	}

	@Override
	protected String getAction() {
		return "scan";
	}

	@Override
	protected String getMessage() {
		return getName();
	}

	public static long scannerFailureSleepTime() {
		return ConfigHelp.reference().getMilliseconds(
				"scanner-task.failure-sleep-time");
	}

	public static long scannerRepositorySleepTime() {
		return ConfigHelp.reference().getMilliseconds(
				"scanner-task.repository-sleep-time");
	}

	public Reporter reporter() {
		return reporter;
	}

}
