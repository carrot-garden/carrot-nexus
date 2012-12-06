/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.util.RatioGauge;

/** size to time */
public class SizeTimeRatio extends RatioGauge {

	private final Counter size;
	private final Timer time;

	public SizeTimeRatio(final Counter size, final Timer time) {
		this.size = size;
		this.time = time;
	}

	@Override
	public double getNumerator() {
		return size.count();
	}

	@Override
	public double getDenominator() {
		return time.sum();
	}

}
