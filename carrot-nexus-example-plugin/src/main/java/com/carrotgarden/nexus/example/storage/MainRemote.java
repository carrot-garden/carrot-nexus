package com.carrotgarden.nexus.example.storage;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

public class MainRemote implements RemoteRepositoryStorage {

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
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReachable(final ProxyRepository repository,
			final ResourceStoreRequest request) throws RemoteAccessException,
			RemoteStorageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URL getAbsoluteUrlFromBase(final ProxyRepository repository,
			final ResourceStoreRequest request) throws RemoteStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validateStorageUrl(final String url)
			throws RemoteStorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsItem(final ProxyRepository repository,
			final ResourceStoreRequest request) throws RemoteAccessException,
			RemoteStorageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsItem(final long newerThen,
			final ProxyRepository repository, final ResourceStoreRequest request)
			throws RemoteAccessException, RemoteStorageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AbstractStorageItem retrieveItem(final ProxyRepository repository,
			final ResourceStoreRequest request, final String baseUrl)
			throws ItemNotFoundException, RemoteAccessException,
			RemoteStorageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeItem(final ProxyRepository repository,
			final StorageItem item)
			throws UnsupportedStorageOperationException, RemoteAccessException,
			RemoteStorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteItem(final ProxyRepository repository,
			final ResourceStoreRequest request) throws ItemNotFoundException,
			UnsupportedStorageOperationException, RemoteAccessException,
			RemoteStorageException {
		// TODO Auto-generated method stub

	}

}
