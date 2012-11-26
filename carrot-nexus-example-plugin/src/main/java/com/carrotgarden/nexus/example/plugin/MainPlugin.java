package com.carrotgarden.nexus.example.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.NexusPlugin;
import org.sonatype.nexus.plugins.PluginContext;

public class MainPlugin implements NexusPlugin {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public void install(final PluginContext context) {

		log.info("###### install ######");

	}

	@Override
	public void init(final PluginContext context) {

		log.info("###### init ######");

	}

	@Override
	public void uninstall(final PluginContext context) {

		log.info("###### uninstall ######");

	}

}
