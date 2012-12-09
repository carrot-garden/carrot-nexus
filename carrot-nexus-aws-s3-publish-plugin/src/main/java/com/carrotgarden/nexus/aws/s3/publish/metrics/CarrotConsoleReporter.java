/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.SortedMap;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.ConsoleReporter;

/**
 * simplified console renderer
 * <p>
 * TODO https://github.com/codahale/metrics/issues/316
 */
public class CarrotConsoleReporter extends ConsoleReporter {

	private final PrintStream out;
	private final MetricPredicate predicate;

	public CarrotConsoleReporter(final MetricsRegistry registry,
			final PrintStream printStream) {

		super(registry, printStream, MetricPredicate.ALL);

		this.out = printStream;
		this.predicate = MetricPredicate.ALL;

	}

	@Override
	public void run() {
		try {
			for (final Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry()
					.groupedMetrics(predicate).entrySet()) {
				out.println();
				out.print(entry.getKey());
				out.println(':');
				for (final Entry<MetricName, Metric> subEntry : entry
						.getValue().entrySet()) {
					out.println();
					out.print("  ");
					out.print(subEntry.getKey().getName());
					out.println(':');
					subEntry.getValue().processWith( //
							this, subEntry.getKey(), out);
				}
			}
			out.println();
			out.flush();
		} catch (final Exception e) {
			e.printStackTrace(out);
		}
	}

	@Override
	public void processCounter(final MetricName name, final Counter counter,
			final PrintStream stream) {
		stream.printf("    count = %,d\n", counter.count());
	}

}
