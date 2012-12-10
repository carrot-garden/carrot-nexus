/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.storage;

import java.io.File;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.FSPeer;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntryList;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigResolver;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigState;
import com.carrotgarden.nexus.aws.s3.publish.mailer.CarrotMailer;
import com.carrotgarden.nexus.aws.s3.publish.mailer.Report;
import com.carrotgarden.nexus.aws.s3.publish.metrics.StorageReporter;
import com.carrotgarden.nexus.aws.s3.publish.util.AmazonHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.yammer.metrics.Metrics;

/**
 * custom local/remote store
 * <p>
 * store both on local file system and on the amazon bucket
 * <p>
 * single file item can be stored to multiple buckets
 * <p>
 * fail if any store operation fails
 */
@Singleton
@Named(CarrotRepositoryStorage.NAME)
public class CarrotRepositoryStorage extends DefaultFSLocalRepositoryStorage
		implements LocalRepositoryStorage {

	public static final String NAME = "carrot.repo.storage";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ConfigResolver resolver;
	private final StorageReporter reporter;
	private final CarrotMailer mailer;

	private final Pattern defaultExclude;

	@Inject
	public CarrotRepositoryStorage( //
			final CarrotMailer mailer, //
			// final StorageReporter reporter, //
			final ConfigResolver resolver, //
			final Wastebasket wastebasket, //
			final LinkPersister linkPersister, //
			final MimeSupport mimeSupport, //
			final FSPeer fsPeer //
	) {

		super(wastebasket, linkPersister, mimeSupport, fsPeer);

		this.mailer = mailer;
		this.resolver = resolver;

		/** use global for now */
		this.reporter = new StorageReporter(Metrics.defaultRegistry());

		defaultExclude = ConfigHelp.defaultExclude();

	}

	@Override
	public String getProviderId() {
		return NAME;
	}

	private boolean isDefaultExclude(final String path) {
		return defaultExclude.matcher(path).matches();
	}

	/**
	 * this receives requests for both artifacts and meta data
	 */
	@Override
	public void storeItem(final Repository repository, final StorageItem any)
			throws UnsupportedStorageOperationException, LocalStorageException {

		try {

			final File file = getFileFromBase(repository,
					any.getResourceStoreRequest());

			reporter.repoFileWatch.add(file);

			final boolean isFileItem = any instanceof StorageFileItem;

			if (!isFileItem) {
				super.storeItem(repository, any);
				reporter.amazonIgnoredFileCount.inc();
				reporter.skipFileWatch.add(file + "[not a file item]");
				return;
			}

			if (isDefaultExclude(any.getPath())) {
				super.storeItem(repository, any);
				reporter.amazonIgnoredFileCount.inc();
				reporter.skipFileWatch.add(file + "[default exclude]");
				return;
			}

			final String repoId = repository.getId();
			final StorageFileItem item = (StorageFileItem) any;
			final String path = item.getPath();

			/** store local */

			super.storeItem(repository, item);

			/** store all remote */

			final ConfigEntryList entryList = resolver.entryList(repoId);

			if (entryList.isEmpty()) {
				reporter.amazonIgnoredFileCount.inc();
				reporter.skipFileWatch.add(file + "[no plugin entries]");
				return;
			}

			boolean isAmazonSaved = true;

			for (final ConfigEntry entry : entryList) {

				if (!entry.isConfigState(ConfigState.ENABLED)) {
					reporter.amazonIgnoredFileCount.inc();
					reporter.skipFileWatch.add(file + "[entry is disabled]");
					continue;
				}

				if (entry.isExcluded(path)) {
					reporter.amazonIgnoredFileCount.inc();
					reporter.skipFileWatch.add(file + "[entry is excluded]");
					continue;
				}

				final AmazonService service = entry.amazonService();

				isAmazonSaved &= AmazonHelp.storeItem( //
						service, repository, item, file, log);

				if (isAmazonSaved) {

					reporter.amazonPublishedFileCount.inc();
					reporter.amazonPublishedFileSize.inc(file.length());

					reporter.saveFileWatch.add(file);

					mailer.sendDeployReport( //
							Report.DEPLOY_SUCCESS, entry, repository, item);

					continue;

				} else {

					reporter.skipFileWatch.add(file + "[amazon failure]");

					mailer.sendDeployReport( //
							Report.DEPLOY_FAILURE, entry, repository, item);

					break;

				}

			}

			if (isAmazonSaved) {
				return;
			}

			/** revert local */

			super.shredItem(repository, item.getResourceStoreRequest());

			final String message = //
			"amazon persist failure;" + //
					" repo=" + repository.getId() + //
					" path=" + path //
			;

			throw new LocalStorageException(message);

		} catch (final Exception e) {

			reporter.amazonFailedFileCount.inc();

			throw new LocalStorageException(e);

		}

	}

}
