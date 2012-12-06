/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.ConsoleReporter;

/**
 * metrics reporter prototype
 */
@Named("base")
public class BaseReporter implements Reporter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();

	private final PrintStream printStream = new PrintStream(arrayStream);

	private final MetricsRegistry registry;

	private final ConsoleReporter reporter;

	@Inject
	public BaseReporter( //
			@Named("reporter") final MetricsRegistry registry //
	) {

		this.registry = registry;

		this.reporter = new CarrotConsoleReporter(registry, printStream);

	}

	@Override
	public MetricsRegistry registry() {
		return registry;
	}

	@Override
	public String report() {

		arrayStream.reset();

		reporter.run();

		return arrayStream.toString();

	}

	@Override
	public void report(final StringBuilder text, final String title) {

		text.append("#####################################\n");
		text.append("###\n");
		text.append("### " + title + "\n");
		text.append("###\n");

		text.append(report());

	}

	protected Counter newCounter(final String name) {
		return registry().newCounter(getClass(), name);
	}

	protected <T> Gauge<T> newGauge(final String name, final Gauge<T> target) {
		return registry().newGauge(getClass(), name, target);
	}

	protected Meter newMeter(final String name, final String type,
			final TimeUnit unit) {
		return registry().newMeter(getClass(), name, type, unit);
	}

}
