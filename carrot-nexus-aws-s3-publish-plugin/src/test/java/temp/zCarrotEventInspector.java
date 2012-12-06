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
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.plexus.appevents.Event;

//@Named
//@Singleton
class zCarrotEventInspector implements EventInspector {

	public static final String NAME = "CarrotEventInspector";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{

		log.info("init " + NAME);

	}

	@Inject
	private zAmazonService amazonService;

	@Override
	public boolean accepts(final Event<?> evt) {

		return false //
				|| evt instanceof RepositoryItemEventStore //
				|| evt instanceof RepositoryItemEventCache //
				|| evt instanceof RepositoryItemEventDelete //
		;

	}

	@Override
	public void inspect(final Event<?> evt) {

		if (evt instanceof RepositoryItemEventStore) {
			process((RepositoryItemEvent) evt, zMode.ADD);
			return;
		}

		if (evt instanceof RepositoryItemEventCache) {
			process((RepositoryItemEvent) evt, zMode.ADD);
			return;
		}

		if (evt instanceof RepositoryItemEventDelete) {
			process((RepositoryItemEvent) evt, zMode.REMOVE);
			return;
		}

	}

	private void process(final RepositoryItemEvent evt, final zMode mode) {

		final StorageItem item = evt.getItem();

		final MavenRepository repo = (MavenRepository) evt.getRepository();

		final String path = item.getRepositoryItemUid().getPath();

		final Gav gav = repo.getGavCalculator().pathToGav(path);

		if (gav == null || gav.isSignature() || gav.isHash()) {
			return;
		}

		if (gav.isSnapshot()) {
			return;
		}

		// log.info("mode={}, path={}", mode, path);

		try {

			// final File file = File.createTempFile("mavent-test", "");

			// amazonService.put(path, file);

		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

}
