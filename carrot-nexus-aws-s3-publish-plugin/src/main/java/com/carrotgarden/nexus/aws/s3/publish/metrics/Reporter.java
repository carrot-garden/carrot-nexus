/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * on demand metrics reporter
 */
public interface Reporter {

	double GB = 1000 * 1000 * 1000;

	/** global reporter */
	Reporter DEFAULT = new BaseReporter(Metrics.defaultRegistry());

	/** registry per reporter */
	MetricsRegistry registry();

	/** produce report on demand */
	String report();

	/** produce report on demand and append to text */
	void report(StringBuilder text, String title);

	/** reset all metrics in registry */
	public void reset();

}
