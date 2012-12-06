/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import static com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp.*;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.sisu.resource.scanner.Scanner;
import org.sonatype.sisu.resource.scanner.helper.ListenerSupport;

import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttribute;
import com.carrotgarden.nexus.aws.s3.publish.util.PathHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.RepoHelp;

//@Singleton
//@Named(CarrotScannerProvider.NAME)
class zCarrotScannerProvider implements zCarrotScanner {

	private class ScannerTask extends ListenerSupport implements Runnable {

		private final Repository repository;

		private File root;

		private ScannerTask(final Repository repository) {
			this.repository = repository;
		}

		@Override
		public void onBegin() {

			// amazonService.checkAvailable();

			log.info("scan init repo={}", repository.getId());
			log.info("found root : " + root);

		}

		@Override
		public void onEnd() {

			log.info("scan done repo={}", repository.getId());

		}

		@Override
		public void onFile(final File file) {

			final String path = PathHelp.rootFullPath(PathHelp.relativePath(root, file));

			// if (isIgnoredPath(path)) {
			// return;
			// }

			final ResourceStoreRequest request = new ResourceStoreRequest(path);

			request.getRequestContext().put(//
					AccessManager.REQUEST_AUTHORIZED, "true");

			final StorageItem item;
			try {
				item = repository.retrieveItem(request);
			} catch (final Exception e) {
				log.error("unexpected", e);
				return;
			}

			final boolean isFile = item instanceof StorageFileItem;

			if (!isFile) {
				return;
			}

			final Attributes attributes = item.getRepositoryItemAttributes();

			final String value = attributes.get(CarrotAttribute.ATTR_IS_SAVED);

			if ("true".equals(value)) {
				return;
			}

			while (true) {

				final boolean isSaved = true //
				// && amazonService.isAvailable() //
				// && storeItem(amazonService, repository,
				// (StorageFileItem) item, file, log) //
				;

				if (isSaved) {
					return;
				} else {
					// sleep(amazonConfig.healthPeriod());
				}

			}

		}

		@Override
		public void run() {

			try {

				root = RepoHelp.repoRoot(config, repository);

				scanner.scan(root, this);

			} catch (final Throwable e) {

				log.error("scan failure", e);

			}

		}

	}

	public static final String NAME = "carrot.scanner";

	// @Inject
	// private AmazonConfig amazonConfig;

	// @Inject
	// private AmazonService amazonService;

	@Inject
	private ApplicationConfiguration config;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ConcurrentMap<String, Repository> repoMap = //
	new ConcurrentHashMap<String, Repository>();

	@Inject
	@Named("serial")
	private Scanner scanner;

	private final ScheduledExecutorService scheduler;

	{

		log.info("init " + NAME);

		final NexusThreadFactory factory = //
		new NexusThreadFactory("carrot", NAME);

		scheduler = Executors.newScheduledThreadPool(1, factory);

	}

	@Override
	public void register(final Repository repository) {

		final String id = repository.getId();

		final Repository previous = repoMap.putIfAbsent(id, repository);

		if (previous != null) {
			log.info(" repo already present: {}", id);
			return;
		}

		final ScannerTask scannerTask = new ScannerTask(repository);

		scheduler.scheduleAtFixedRate(scannerTask, 5, 5, TimeUnit.SECONDS);

		log.info("registered repo : {}", id);

	}

	private void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final Exception e) {
			log.info("interrupted");
			return;
		}
	}

}
