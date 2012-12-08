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
import it.util.Ready;
import it.util.TestHelp;

import java.io.File;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotgarden.nexus.aws.s3.publish.config.Form;

public class TestScanner extends TestAny {

	protected static final Logger log = LoggerFactory
			.getLogger(TestScanner.class);

	public TestScanner(final String nexusBundleCoordinates) {
		super(nexusBundleCoordinates);
	}

	@Test
	public void testScanner() throws Exception {

		/**
		 * short scanner period, enabled
		 */
		final Map<String, String> props = Form.propsFrom(TestHelp.configFile());
		props.put("combo-id", repoId());
		props.put("enable-scanner", "true");
		props.put("scanner-schedule", "0/3 * * * * ? *");

		applyConfig(true, props);

		testScanner("scan/scan/1.0/scan-1.0.pom");
		testScanner("scan/scan/1.0/scan-1.0.jar");
		testScanner("scan/scan/1.0/scan-1.0.zip");

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

		final File amazon = fileTransient();

		final Ready ready = new Ready() {
			@Override
			public boolean isReady() {
				return amazonService().load(path, amazon);
			}
		};
		TestHelp.sleep(10 * 1000, ready);

		assertTrue("source equals amazon", TestHelp.isSameFile(source, amazon));
		assertTrue("amazon delete", amazonService().kill(path));

	}

}
