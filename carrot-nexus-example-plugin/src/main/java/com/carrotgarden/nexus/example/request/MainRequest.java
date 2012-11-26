package com.carrotgarden.nexus.example.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;

public class MainRequest implements RequestProcessor {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public boolean process(final Repository repository,
			final ResourceStoreRequest request, final Action action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldProxy(final ProxyRepository repository,
			final ResourceStoreRequest request) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldCache(final ProxyRepository repository,
			final AbstractStorageItem item) {
		// TODO Auto-generated method stub
		return false;
	}

}
