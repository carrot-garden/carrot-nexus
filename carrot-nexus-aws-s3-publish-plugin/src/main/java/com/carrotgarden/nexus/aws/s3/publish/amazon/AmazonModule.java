package com.carrotgarden.nexus.aws.s3.publish.amazon;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * amazon factory setup
 */
@Named
public class AmazonModule extends AbstractModule {

	@Override
	protected void configure() {

		install(new FactoryModuleBuilder().implement(AmazonManager.class,
				AmazonServiceProvider.class).build(AmazonFactory.class));

	}

}
