/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import javax.inject.Inject;

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

//@Named
//@Singleton
public class zCarrotRequestProcessor implements RequestProcessor {

	public static final String NAME = "CarrotRequestProcessor";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("### init " + NAME);
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
