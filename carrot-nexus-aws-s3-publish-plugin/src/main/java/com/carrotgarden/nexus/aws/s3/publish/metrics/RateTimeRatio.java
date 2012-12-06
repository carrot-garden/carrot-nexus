/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.util.RatioGauge;

/**
 * rate to time
 */
public class RateTimeRatio extends RatioGauge {

	private final Meter rate;
	private final Timer time;

	public RateTimeRatio(final Meter rate, final Timer time) {
		this.rate = rate;
		this.time = time;
	}

	@Override
	public double getNumerator() {
		return rate.oneMinuteRate();
	}

	@Override
	public double getDenominator() {
		return time.oneMinuteRate();
	}

}
