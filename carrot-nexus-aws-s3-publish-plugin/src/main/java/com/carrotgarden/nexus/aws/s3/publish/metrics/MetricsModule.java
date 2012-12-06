/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.yammer.metrics.guice.InstrumentationModule;

/**
 * activate metrics annotations
 * <p>
 * use of annotations problematic due to singleton registry assumption.
 * <p>
 * TODO need another custom module
 */
@Named
public class MetricsModule extends AbstractModule {

	@Override
	protected void configure() {

		install(new InstrumentationModule());

	}

}
