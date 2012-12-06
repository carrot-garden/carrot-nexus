/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import javax.inject.Inject;
import javax.inject.Named;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonProvider;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * {@link AmazonProvider} reporter
 */
@Named("amazon")
public class AmazonReporter extends BaseReporter {

	public final Counter requestTotalCount;
	public final Counter requestLoadCount;
	public final Counter requestSaveCount;
	public final Counter requestKillCount;
	public final Counter requestCheckCount;
	public final Counter requestFailedCount;
	public final Counter publishedFileSize;
	public final PeekValueGuage<Boolean> providerAvailable = new PeekValueGuage<Boolean>();

	@Inject
	public AmazonReporter( //
			@Named("reporter") final MetricsRegistry registry //
	) {

		super(registry);

		//

		requestTotalCount = newCounter("amazon rquests total");
		requestFailedCount = newCounter("amazon rquests failed");
		requestLoadCount = newCounter("amazon 'load/get' requests");
		requestSaveCount = newCounter("amazon 'save/put' requests");
		requestKillCount = newCounter("amazon 'kill/delete' requests");
		requestCheckCount = newCounter("amazon 'check/get' requests");

		//

		publishedFileSize = newCounter("published file size");

		//

		newGauge("provider is available", providerAvailable);

		newGauge("estimated amazon monthly storage cost", new Gauge<Double>() {
			@Override
			public Double value() {
				final double perGB = ConfigHelp.reference().getDouble(
						"amazon-provider.cost.gigabyte-storage");
				final long size = publishedFileSize.count();
				return perGB * (size / GB);
			}
		});

	}

}
