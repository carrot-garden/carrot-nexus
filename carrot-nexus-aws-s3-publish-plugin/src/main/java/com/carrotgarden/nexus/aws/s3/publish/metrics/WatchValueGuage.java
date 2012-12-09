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
public class WatchValueGuage<T> extends Gauge<T> implements Clearable {

	private T value;

	@Override
	public T value() {
		return value;
	}

	public void value(final T value) {
		this.value = value;
	}

	@Override
	public void clear() {
		value = null;
	}

}
