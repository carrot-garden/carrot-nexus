package com.carrotgarden.nexus.example.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.nexus.rest.NexusApplicationCustomizer;
import org.sonatype.plexus.rest.RetargetableRestlet;

public class MainApp implements NexusApplicationCustomizer {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public void customize(final NexusApplication argapp0,
			final RetargetableRestlet restlet) {
		// TODO Auto-generated method stub

	}

}
