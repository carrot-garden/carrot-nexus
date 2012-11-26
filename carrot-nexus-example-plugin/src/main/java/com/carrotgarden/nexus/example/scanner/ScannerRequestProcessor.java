package com.carrotgarden.nexus.example.scanner;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

import com.carrotgarden.nexus.example.event.ScannerEvent;

@Named(ScannerRequestProcessor.NAME)
public class ScannerRequestProcessor implements RequestProcessor {

	public static final String NAME = "virusScanner";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Inject
	private ApplicationEventMulticaster eventer;

	@Inject
	@Named(ScannerABC.NAME)
	private Scanner scanner;

	// @Inject
	// private @Named("A") CommonDependency commonDependency;

	@Override
	public boolean process(final Repository repository,
			final ResourceStoreRequest request, final Action action) {

		final String path = request.getRequestPath();

		// Check dependency
		// System.out.println(
		// "VirusScannerRequestProcessor --- CommonDependency data: " +
		// commonDependency.getData()
		// );

		// don't decide until have content

		return true;

	}

	@Override
	public boolean shouldProxy(final ProxyRepository repository,
			final ResourceStoreRequest request) {

		// don't decide until have content

		return true;

	}

	@Override
	public boolean shouldCache(final ProxyRepository repository,
			final AbstractStorageItem item) {

		if (item instanceof StorageFileItem) {

			final StorageFileItem file = (StorageFileItem) item;

			// do a virus scan
			final boolean hasVirus = scanner.hasVirus(file);

			if (hasVirus) {
				eventer.notifyEventListeners( //
				new ScannerEvent(item.getRepositoryItemUid().getRepository(),
						file));
			}

			return !hasVirus;

		} else {

			return true;

		}

	}

}
