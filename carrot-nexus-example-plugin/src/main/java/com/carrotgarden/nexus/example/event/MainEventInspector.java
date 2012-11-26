package com.carrotgarden.nexus.example.event;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

@Named(MainEventInspector.NAME)
public class MainEventInspector implements EventInspector {

	public static final String NAME = "event-instpector";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public boolean accepts(final Event<?> evt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void inspect(final Event<?> evt) {
		// TODO Auto-generated method stub

	}

}
