/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import com.yammer.metrics.core.Gauge;

/** inverse gauge */
public class PeekBooleanGuage extends Gauge<Boolean> {

	private boolean value;

	@Override
	public Boolean value() {
		return value;
	}

	public void value(final boolean value) {
		this.value = value;
	}

}
