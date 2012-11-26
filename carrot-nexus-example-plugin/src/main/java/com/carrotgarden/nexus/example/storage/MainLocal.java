package com.carrotgarden.nexus.example.storage;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreIteratorRequest;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

public class MainLocal implements LocalRepositoryStorage {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public String getProviderId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validateStorageUrl(final String url)
			throws LocalStorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isReachable(final Repository repository,
			final ResourceStoreRequest request) throws LocalStorageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URL getAbsoluteUrlFromBase(final Repository repository,
			final ResourceStoreRequest request) throws LocalStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsItem(final Repository repository,
			final ResourceStoreRequest request) throws LocalStorageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AbstractStorageItem retrieveItem(final Repository repository,
			final ResourceStoreRequest request) throws ItemNotFoundException,
			LocalStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeItem(final Repository repository, final StorageItem item)
			throws UnsupportedStorageOperationException, LocalStorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteItem(final Repository repository,
			final ResourceStoreRequest request) throws ItemNotFoundException,
			UnsupportedStorageOperationException, LocalStorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public void shredItem(final Repository repository,
			final ResourceStoreRequest request) throws ItemNotFoundException,
			UnsupportedStorageOperationException, LocalStorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveItem(final Repository repository,
			final ResourceStoreRequest from, final ResourceStoreRequest to)
			throws ItemNotFoundException, UnsupportedStorageOperationException,
			LocalStorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<StorageItem> listItems(final Repository repository,
			final ResourceStoreRequest request) throws ItemNotFoundException,
			LocalStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<StorageItem> iterateItems(final Repository repository,
			final ResourceStoreIteratorRequest request)
			throws ItemNotFoundException, LocalStorageException {
		// TODO Auto-generated method stub
		return null;
	}

}
