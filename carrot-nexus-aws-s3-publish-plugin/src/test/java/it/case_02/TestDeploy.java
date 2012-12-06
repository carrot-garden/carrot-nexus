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

import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;

import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttribute;
import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttributeBean;

public class TestDeploy extends TestAny {

	public TestDeploy(final String nexusBundleCoordinates) {
		super(nexusBundleCoordinates);
	}

	@Test
	public void testActivate() throws Exception {

		applyConfigDefault();

		final CapabilityListItemResource entry1 = resourceEntry();

		assertFalse("plugin is disabled", entry1.isEnabled());
		assertFalse("plugin is passivated", entry1.isActive());

		applyConfigCustom();

		final CapabilityListItemResource entry2 = resourceEntry();

		assertTrue("plugin is enabled", entry2.isEnabled());
		assertTrue("plugin is activated", entry2.isActive());

	}

	@Test
	public void testDeploySkip() throws Exception {

		applyConfigDefault();

		final CapabilityListItemResource entry = resourceEntry();

		assertFalse("plugin is disabled", entry.isEnabled());
		assertFalse("plugin is passivated", entry.isActive());

		testDeploySkip("invalid/invalid/1.1.1/invalid-1.1.1.pom");
		testDeploySkip("invalid/invalid/1.1.2/invalid-1.1.2.jar");
		testDeploySkip("invalid/invalid/1.1.3/invalid-1.1.3.zip");

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

	}

	@Test
	public void testDeployWork() throws Exception {

		applyConfigCustom();

		testDeployWork("junit/junit/3.8.1/junit-3.8.1.pom");
		testDeployWork("junit/junit/3.8.1/junit-3.8.1.jar");
		testDeployWork("junit/junit/3.8.1/junit-3.8.1.zip");

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

	}

}
