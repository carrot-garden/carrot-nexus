package com.carrotgarden.nexus.aws.s3.publish.amazon;

import javax.inject.Inject;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class AmazonImpl implements Amazon, Configurable {

	@Inject
	private ApplicationConfiguration config;

	@Override
	public CoreConfiguration getCurrentCoreConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configure(final Object config) throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean commitChanges() throws ConfigurationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rollbackChanges() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
