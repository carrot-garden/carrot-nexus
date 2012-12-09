/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;
import com.carrotgarden.nexus.aws.s3.publish.config.Form;

public class TestReferenceConf {

	protected final static Logger log = LoggerFactory
			.getLogger(TestReferenceConf.class);

	@Test
	public void testExclude() throws Exception {

		final Map<String, String> props = Form.propsDefault();

		final ConfigBean configBean = new ConfigBean(props);

		assertTrue(configBean.enableExclude());

		final String exclude = configBean.excludePattern();

		// log.info("exclude : {}", exclude);

		final Pattern pattern = Pattern.compile(configBean.excludePattern());

		//

		assertFalse(pattern
				.matcher(
						"com/carrotgarden/test/carrot-test/1.0.3/carrot-test-1.0.3.jar")
				.matches());

		//

		assertTrue(pattern
				.matcher(
						"com/carrotgarden/test/carrot-test/1.0.3/carrot-test-1.0.3-sources.jar")
				.matches());

		assertTrue(pattern
				.matcher(
						"com/carrotgarden/test/carrot-test/1.0.3/carrot-test-1.0.3-javadoc.jar")
				.matches());

		assertTrue(pattern
				.matcher(
						"com/carrotgarden/test/carrot-test/1.0.3/carrot-test-1.0.3.jar.asc")
				.matches());
		assertTrue(pattern
				.matcher(
						"com/carrotgarden/test/carrot-test/1.0.3/carrot-test-1.0.3.jar.md5")
				.matches());
		assertTrue(pattern
				.matcher(
						"com/carrotgarden/test/carrot-test/1.0.3/carrot-test-1.0.3.jar.sha1")
				.matches());

	}

}
