/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.case_02;

import static org.junit.Assert.*;
import it.any.TestAny;

import java.io.File;

import org.junit.Test;

public class TestScanner extends TestAny {

	public TestScanner(final String nexusBundleCoordinates) {
		super(nexusBundleCoordinates);
	}

	@Test
	public void testScanner() throws Exception {

		applyConfigCustom();

		testScanner("scanner/scanner/1.0/scanner-1.0.pom");

	}

	private void testScanner(final String path) throws Exception {

		final File source = fileSource(path);
		final File target = fileTarget(path);
		final File attrib = fileAttrib(path);

		assertFalse("source present", source.exists());
		assertFalse("target missing", target.exists());
		assertFalse("attrib missing", attrib.exists());

	}

}
