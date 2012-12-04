/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.storage;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
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
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigAction;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntryList;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigRegistry;
import com.carrotgarden.nexus.aws.s3.publish.util.Util;

/**
 * custom local store
 * <p>
 * store both on local file system and on amazon bucket
 * <p>
 * fail if any store operation fails
 */
@Singleton
@Named(CarrotStorageProvider.NAME)
public class CarrotStorageProvider extends DefaultFSLocalRepositoryStorage
		implements LocalRepositoryStorage {

	public static final String NAME = "carrot.repo.storage";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("init " + NAME);
	}

	@Inject
	private ConfigRegistry configRegistry;

	@Inject
	public CarrotStorageProvider(final Wastebasket wastebasket,
			final LinkPersister linkPersister, final MimeSupport mimeSupport,
			final FSPeer fsPeer) {

		super(wastebasket, linkPersister, mimeSupport, fsPeer);

	}

	@Override
	public String getProviderId() {
		return NAME;
	}

	@Override
	public void storeItem(final Repository repository, final StorageItem item)
			throws UnsupportedStorageOperationException, LocalStorageException {

		final boolean isFileItem = item instanceof StorageFileItem;

		if (!isFileItem) {
			super.storeItem(repository, item);
			return;
		}

		final String repoId = repository.getId();

		log.info("\n\t ### repo-item {}::{}", repoId, item.getPath());

		final ConfigAction action = configRegistry.action(repoId);

		log.info("\n\t ### action : {}", action);

		switch (action) {
		case FAIL:
			throw new LocalStorageException("amazon provider not ready");
		case SKIP:
			super.storeItem(repository, item);
			return;
		case WORK:
			break;
		}

		try {

			final ResourceStoreRequest request = item.getResourceStoreRequest();

			final File file = getFileFromBase(repository, request);

			/** store local */

			super.storeItem(repository, item);

			/** store remote */

			final ConfigEntryList entryList = configRegistry.entryList(repoId);

			boolean isSaved = true;

			for (final ConfigEntry entry : entryList) {

				final AmazonService amazonService = entry.amazonService();

				isSaved &= Util.storeItem(amazonService, repository,
						(StorageFileItem) item, file, log);
			}

			if (isSaved) {
				return;
			}

			/** revert local */
			super.shredItem(repository, request);

			throw new LocalStorageException("amazon store failure");

		} catch (final Exception e) {
			log.error("store failure", e);
			throw new LocalStorageException(e);
		}

	}

}
