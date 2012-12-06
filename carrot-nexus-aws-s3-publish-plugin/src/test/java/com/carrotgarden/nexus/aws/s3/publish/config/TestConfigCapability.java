/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.M2GavCalculator;

public class TestConfigCapability {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGAV() {

		final M2GavCalculator calc = new M2GavCalculator();

		{
			final String path = "/junit/junit/3.8.1/junit-3.8.1.pom";
			final Gav gav = calc.pathToGav(path);
			log(path, gav);
			assertFalse(gav.isSnapshot());
		}

		{
			final String path = "junit/junit/1.4.3-SNAPSHOT/junit-1.4.3-20120912.150156-11.pom";
			final Gav gav = calc.pathToGav(path);
			log(path, gav);
			assertTrue(gav.isSnapshot());
		}

		{
			final String path = "junit/junit/1.4.3-SNAPSHOT/junit-1.4.3-SNAPSHOT.pom";
			final Gav gav = calc.pathToGav(path);
			log(path, gav);
			assertTrue(gav.isSnapshot());
		}

		{
			final String path = "junit/junit/hello/junit-1.4.3-SNAPSHOT.pom";
			final Gav gav = calc.pathToGav(path);
			log(path, gav);
			assertNull(gav);
		}

		{
			final String path = "/.nexus/junit/junit/3.8.1/junit-3.8.1.pom";
			final Gav gav = calc.pathToGav(path);
			log(path, gav);
			assertNotNull(gav);
		}

		{
			final String path = "/junit/junit/3.8.1/junit-3.8.1.pom.sha1";
			final Gav gav = calc.pathToGav(path);
			log(path, gav);
			assertTrue(gav.isHash());
		}

		{
			final String path = "/junit/junit/3.8.1/junit-3.8.1.pom.md5";
			final Gav gav = calc.pathToGav(path);
			log(path, gav);
			assertTrue(gav.isHash());
			assertFalse(gav.isSnapshot());
		}

		{
			final String path = "/junit/junit/3.8.1-SNAPSHOT/junit-3.8.1-SNAPSHOT.pom.asc";
			final Gav gav = calc.pathToGav(path);
			log(path, gav);
			assertTrue(gav.isSignature());
			assertTrue(gav.isSnapshot());
		}

	}

	private void log(final String path, final Gav gav) {

		log.info("#########################################");
		log.info("path    = {}", path);

		if (gav == null) {
			log.info("gav == null");
			return;
		}

		log.info("gav GroupId     = {}", gav.getGroupId());
		log.info("gav ArtifactId  = {}", gav.getArtifactId());
		log.info("gav Version     = {}", gav.getVersion());
		log.info("gav isSnapshot  = {}", gav.isSnapshot());
		log.info("gav isSignature = {}", gav.isSignature());
		log.info("gav isHash      = {}", gav.isHash());

	}

}
