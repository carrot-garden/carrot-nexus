/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValue;

public class TestConfigBean {

	protected final static Logger log = LoggerFactory
			.getLogger(TestConfigBean.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoad() {

		final Map<String, String> props = new TreeMap<String, String>(
				ConfigBean.defaultProps());

		final Set<Entry<String, String>> entrySet = props.entrySet();

		for (final Entry<String, String> entry : entrySet) {

			final String key = entry.getKey();
			final String value = entry.getValue();

			log.info("{}={}", key, value);

		}

	}

	@Test
	public void testOrder() {

		final Config reference = ConfigFactory.defaultReference();

		final Config form = reference.getConfig("form-field");

		final Set<Entry<String, ConfigValue>> entrySet = form.root().entrySet();

		for (final Entry<String, ConfigValue> entry : entrySet) {

			final String key = entry.getKey();
			final ConfigValue value = entry.getValue();

			final ConfigOrigin origin = form.getConfig(key).origin();

			log.info("{}={}", key, origin.lineNumber());

		}

	}

}
