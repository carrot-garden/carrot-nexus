package com.carrotgarden.nexus.aws.s3.publish.storage;

import java.io.File;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
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
import com.carrotgarden.nexus.aws.s3.publish.util.Util;

/**
 * custom local store
 * <p>
 * store both on local file system and on amazon bucket
 * <p>
 * fail if any store operation fails
 */
@Component( //
role = LocalRepositoryStorage.class, //
hint = CarrotStorageProvider.NAME //
)
public class CarrotStorageProvider extends DefaultFSLocalRepositoryStorage
		implements LocalRepositoryStorage {

	public static final String NAME = "carrot-repo-storage";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{

		log.info("init " + NAME);

	}

	@Inject
	private AmazonService amazonService;

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

		if (item instanceof StorageFileItem) {

			try {

				final ResourceStoreRequest request = item
						.getResourceStoreRequest();

				final File file = getFileFromBase(repository, request);

				/** store local */
				super.storeItem(repository, item);

				/** store remote */
				final boolean isSaved = Util.storeItem(amazonService,
						repository, (StorageFileItem) item, file, log);

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

		} else {

			super.storeItem(repository, item);

		}

	}

}
