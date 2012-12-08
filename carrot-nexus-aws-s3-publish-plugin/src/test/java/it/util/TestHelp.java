/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.util;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.test.utils.FileTestingUtils;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigDescriptor;

public class TestHelp {

	protected static final Logger log = LoggerFactory.getLogger(TestHelp.class);

	public static boolean isOrdered(final long... source) {
		final long[] target = source.clone();
		Arrays.sort(target);
		return Arrays.equals(source, target);
	}

	public static boolean isSameFile(final File source, final File target)
			throws Exception {
		return FileTestingUtils.compareFileSHA1s(source, target);
	}

	public static void sleep(final long millis) {
		final Ready never = new Ready() {
			@Override
			public boolean isReady() {
				return false;
			}

		};
		sleep(millis, 10, never);
	}

	public static void sleep(final long millis, final int steps,
			final Ready state) {
		for (int k = 0; k < steps; k++) {
			if (state.isReady()) {
				return;
			}
			try {
				log.info("sleep {}/{}", k * millis / steps, millis);
				Thread.sleep(millis / steps);
			} catch (final Exception e) {
				return;
			}
		}
	}

	public static void sleep(final long millis, final Ready state) {
		sleep(millis, 10, state);
	}

	/** config file with amazon access */
	public static File configFile() {

		final String home = System.getProperty("user.home");

		final File folder = new File(home, ".amazon/carrotgarden");

		final File file = new File(folder, ConfigDescriptor.NAME + ".conf");

		assertTrue(file.exists());

		return file;

	}

}
