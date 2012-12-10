/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.util;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfigHelp {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testPatternSpecial01() {
		final Pattern pattern = Pattern.compile("");
		assertFalse(pattern.matcher("/").matches());
	}

	@Test
	public void testPatternSpecial02() {
		final Pattern pattern = Pattern.compile("");
		assertFalse(pattern.matcher("/").matches());
	}

	@Test
	public void testPatternDefault() {

		final Pattern exclude = ConfigHelp.defaultExclude();
		final Pattern include = ConfigHelp.defaultInclude();

		log.info("exclude {}", exclude);
		log.info("include {}", include);

		assertNotNull("default exclude pattern", exclude);
		assertNotNull("default include pattern", include);

		assertFalse(include.matcher("maven-metadata.xml").matches());
		assertTrue(include.matcher("/maven-metadata.xml").matches());
		assertTrue(include.matcher("/com/espertech/esper/maven-metadata.xml")
				.matches());

	}

}
