/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import com.yammer.metrics.core.Gauge;

/** inverse gauge */
public class WatchQueueGuage extends Gauge<String> implements Clearable {

	private final int size;
	private final CircularFifoBuffer buffer;

	public void add(final Object item) {
		buffer.add(item);
	}

	public WatchQueueGuage() {
		this(20);
	}

	public WatchQueueGuage(final int size) {
		this.size = size;
		this.buffer = new CircularFifoBuffer(size);
	}

	@Override
	public void clear() {
		buffer.clear();
	}

	@Override
	public String value() {

		final StringBuilder text = new StringBuilder(128);

		if (buffer.isEmpty()) {
			text.append("empty queue");
		} else {
			text.append("last ");
			text.append(size);
			text.append("\n");
			for (final Object item : buffer) {
				text.append(item);
				text.append("\n");
			}
		}

		return text.toString();

	}

}
