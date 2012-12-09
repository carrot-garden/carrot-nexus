/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import com.carrotgarden.nexus.aws.s3.publish.task.ScannerTask;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * {@link ScannerTask} reporter
 */
@Named("task")
public class TaskReporter extends StorageReporter {

	public final Counter scanCount;
	public final Meter scanRate;
	public final Counter repoFileCount;
	public final Counter repoFileSize;
	public final Counter taskRunCount;
	public final Counter amazonExistingFileCount;

	@Inject
	public TaskReporter( //
			@Named("reporter") final MetricsRegistry registry //
	) {

		super(registry);

		//

		amazonExistingFileCount = newCounter("amazon existing file count");

		//

		scanCount = newCounter("scanner file count");
		scanRate = newMeter("scanner file rate", "files", TimeUnit.SECONDS);

		//

		taskRunCount = newCounter("number of times a task was invoked");

		//

		repoFileCount = newCounter("repository file count (stored on amazon)");
		repoFileSize = newCounter("repository file size (stored on amazon)");

		//

		newGauge("repository storage cost (stored on amazon, $/month)",
				new Gauge<Double>() {
					@Override
					public Double value() {
						final double perGB = ConfigHelp.reference().getDouble(
								"amazon-provider.cost.gigabyte-storage");
						final long size = repoFileSize.count();
						return perGB * (size / GB);
					}
				});

	}

}
