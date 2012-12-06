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
import it.util.TestHelp;

import java.io.File;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import com.carrotgarden.nexus.aws.s3.publish.config.Form;

public class TestScanner extends TestAny {

	public TestScanner(final String nexusBundleCoordinates) {
		super(nexusBundleCoordinates);
	}

	@Test
	public void testScanner() throws Exception {

		/** ensure 3 second scanner period */
		final Map<String, String> props = Form.propsFrom(TestHelp.configFile());
		props.put("combo-id", repoId());
		props.put("enable-scanner", "true");
		props.put("scanner-schedule", "0/3 * * * * ? *");

		applyConfig(true, props);

		testScanner("scanner/scanner/1.0/scanner-1.0.pom");

	}

	private void testScanner(final String path) throws Exception {

		assertTrue("amazon delete", amazonService().kill(path));

		final File source = fileSource(path);
		final File target = fileTarget(path);
		final File attrib = fileAttrib(path);

		assertTrue("source present", source.exists());
		assertFalse("target missing", target.exists());
		assertFalse("attrib missing", attrib.exists());

		FileUtils.copyFile(source, target);

		assertTrue("target present", target.exists());

		/** wait for more then 2x3 seconds */
		inspector().waitForCalmPeriod(7 * 1000);

		final File amazon = fileTransient();

		assertTrue("amazon retrieve", amazonService().load(path, amazon));

	}

}
