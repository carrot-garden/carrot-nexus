/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import java.util.Locale;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCarrotConsoleReporter {

	protected final static Logger log = LoggerFactory
			.getLogger(TestCarrotConsoleReporter.class);

	@Test
	public void testFormat() throws Exception {

		log.info("{}", String.format(Locale.US, "%,d", 1234567890));
		log.info("{}", String.format(Locale.GERMAN, "%,d", 1234567890));

	}

}
