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

import org.apache.maven.wagon.TransferFailedException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;

import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttribute;
import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttributeBean;
import com.carrotgarden.nexus.aws.s3.publish.config.Form;

public class TestDeploy extends TestAny {

	protected static final Logger log = LoggerFactory
			.getLogger(TestDeploy.class);

	public TestDeploy(final String nexusBundleCoordinates) {
		super(nexusBundleCoordinates);
	}

	@Test
	public void testDeployFail() throws Exception {

		/**
		 * invalid access, no scanner, enabled
		 */
		final Map<String, String> props = Form.propsFrom(TestHelp.configFile());
		props.put("combo-id", repoId());
		props.put("aws-access", "invalid-access");
		props.put("aws-secret", "invalid-secret");
		props.put("enable-scanner", "false");
		applyConfig(true, props);

		final CapabilityListItemResource entry = resourceEntry();

		assertTrue("plugin is enabled", entry.isEnabled());
		assertTrue("plugin is activated", entry.isActive());

		testDeployFail("fail/fail/1.0/fail-1.0.pom");
		testDeployFail("fail/fail/1.0/fail-1.0.jar");
		testDeployFail("fail/fail/1.0/fail-1.0.zip");

	}

	private void testDeployFail(final String path) throws Exception {

		assertTrue("amazon delete", amazonService().kill(path));

		final File source = fileSource(path);
		final File target = fileTarget(path);
		final File attrib = fileAttrib(path);

		assertTrue("source present", source.exists());
		assertFalse("target missing", target.exists());
		assertFalse("attrib missing", attrib.exists());

		TransferFailedException failure = null;

		try {
			deploy(path);
		} catch (final TransferFailedException e) {
			failure = e;
		}

		assertNotNull(failure);

		log.info("failure : {}", failure.getMessage());

		inspector().waitForCalmPeriod(500);

		assertFalse("target missing", target.exists());
		assertFalse("attrib missing", attrib.exists());

		final File amazon = fileTransient();

		assertFalse("amazon is not retrieved",
				amazonService().load(path, amazon));

		assertTrue("amazon delete", amazonService().kill(path));

	}

	@Test
	public void testDeploySkip() throws Exception {

		/**
		 * valid access, no scanner, disabled
		 */
		final Map<String, String> props = Form.propsFrom(TestHelp.configFile());
		props.put("combo-id", repoId());
		props.put("enable-scanner", "false");
		applyConfig(false, props);

		final CapabilityListItemResource entry = resourceEntry();

		assertFalse("plugin is disabled", entry.isEnabled());
		assertFalse("plugin is passivated", entry.isActive());

		testDeploySkip("skip/skip/1.1.1/skip-1.1.1.pom");
		testDeploySkip("skip/skip/1.1.2/skip-1.1.2.jar");
		testDeploySkip("skip/skip/1.1.3/skip-1.1.3.zip");

	}

	private void testDeploySkip(final String path) throws Exception {

		assertTrue("amazon delete", amazonService().kill(path));

		final File source = fileSource(path);
		final File target = fileTarget(path);
		final File attrib = fileAttrib(path);

		assertTrue("source present", source.exists());
		assertFalse("target missing", target.exists());
		assertFalse("attrib missing", attrib.exists());

		deploy(path);

		inspector().waitForCalmPeriod(500);

		assertTrue("target present", target.exists());
		assertTrue("attrib present", attrib.exists());
		assertTrue("source equals target", TestHelp.isSameFile(source, target));

		final CarrotAttribute carrot = CarrotAttributeBean.fromJson(attrib);

		assertFalse("attrib is not set", carrot.isSaved());

		final File amazon = fileTransient();

		assertFalse("amazon is not retrieved",
				amazonService().load(path, amazon));

		assertTrue("amazon delete", amazonService().kill(path));

	}

	@Test
	public void testDeployWork() throws Exception {

		/**
		 * valid access, no scanner, enabled
		 */
		final Map<String, String> props = Form.propsFrom(TestHelp.configFile());
		props.put("combo-id", repoId());
		props.put("enable-scanner", "false");
		applyConfig(true, props);

		testDeployWork("work/work/3.8.1/work-3.8.1.pom");
		testDeployWork("work/work/3.8.1/work-3.8.1.jar");
		testDeployWork("work/work/3.8.1/work-3.8.1.zip");

	}

	private void testDeployWork(final String path) throws Exception {

		assertTrue("amazon delete", amazonService().kill(path));

		final File source = fileSource(path);
		final File target = fileTarget(path);
		final File attrib = fileAttrib(path);

		assertTrue("source present", source.exists());
		assertFalse("target missing", target.exists());
		assertFalse("attrib missing", attrib.exists());

		final long timeStart = System.currentTimeMillis();

		deploy(path);

		inspector().waitForCalmPeriod(500);

		final long timeFinish = System.currentTimeMillis();

		assertTrue("target present", target.exists());
		assertTrue("attrib present", attrib.exists());
		assertTrue("source equals target", TestHelp.isSameFile(source, target));

		final CarrotAttribute carrot = CarrotAttributeBean.fromJson(attrib);

		assertTrue("attrib is set", carrot.isSaved());
		assertTrue("attrib time in range",
				TestHelp.isOrdered(timeStart, carrot.saveTime(), timeFinish));

		final File amazon = fileTransient();

		assertTrue("amazon retrieve", amazonService().load(path, amazon));
		assertTrue("source equals amazon", TestHelp.isSameFile(source, amazon));
		assertTrue("amazon delete", amazonService().kill(path));

	}

}
