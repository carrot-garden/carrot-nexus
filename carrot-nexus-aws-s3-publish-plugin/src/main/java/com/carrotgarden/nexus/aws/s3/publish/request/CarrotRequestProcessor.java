package com.carrotgarden.nexus.aws.s3.publish.request;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

@Named(CarrotRequestProcessor.NAME)
public class CarrotRequestProcessor implements RequestProcessor {

	public static final String NAME = "CarrotRequestProcessor";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Inject
	private ApplicationEventMulticaster eventer;

	@Inject
	private ApplicationConfiguration config;

	@Override
	public boolean process(final Repository repository,
			final ResourceStoreRequest request, final Action action) {

		return true;

	}

	@Override
	public boolean shouldProxy(final ProxyRepository repository,
			final ResourceStoreRequest request) {

		return true;

	}

	@Override
	public boolean shouldCache(final ProxyRepository repository,
			final AbstractStorageItem item) {

		return true;

	}

}
