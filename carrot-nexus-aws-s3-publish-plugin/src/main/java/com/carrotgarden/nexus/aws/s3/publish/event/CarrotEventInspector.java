package com.carrotgarden.nexus.aws.s3.publish.event;

import javax.inject.Named;

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

@Named(CarrotEventInspector.NAME)
public class CarrotEventInspector implements EventInspector {

	public static final String NAME = "CarrotEventInspector";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

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
			process((RepositoryItemEvent) evt, Mode.ADD);
			return;
		}

		if (evt instanceof RepositoryItemEventCache) {
			process((RepositoryItemEvent) evt, Mode.ADD);
			return;
		}

		if (evt instanceof RepositoryItemEventDelete) {
			process((RepositoryItemEvent) evt, Mode.REMOVE);
			return;
		}

	}

	private void process(final RepositoryItemEvent evt, final Mode mode) {

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

		log.info("mode={}, path={}", mode, path);

	}

}
