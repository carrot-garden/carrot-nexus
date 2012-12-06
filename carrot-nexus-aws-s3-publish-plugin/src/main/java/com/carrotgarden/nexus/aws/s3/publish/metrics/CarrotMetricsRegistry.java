/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import javax.inject.Named;

import com.yammer.metrics.core.MetricsRegistry;

/**
 * multiton for injection
 * <p>
 * TODO https://github.com/codahale/metrics/issues/317
 */
@Named("reporter")
public class CarrotMetricsRegistry extends MetricsRegistry implements Clearable {

	@Override
	public void clear() {

	}

}
