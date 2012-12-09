/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.util;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 */
public class ConfigHelp {

	public static Map<String, String> fromJson(final File file)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		@SuppressWarnings("unchecked")
		final Map<String, String> map = mapper.readValue(file, Map.class);

		return map;

	}

	public static Config reference() {
		final ClassLoader loader = ConfigHelp.class.getClassLoader();
		return ConfigFactory.defaultReference(loader);
	}

	public static Map<String, String> jsonFrom(final File file)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		@SuppressWarnings("unchecked")
		final Map<String, String> map = mapper.readValue(file, Map.class);

		return map;

	}

	public static String pluginName() {
		return reference().getString("plugin-name");
	}

	public static Pattern defaultExclude() {
		try {
			final String pattern = //
			ConfigHelp.reference().getString("exclude-pattern");
			return Pattern.compile(pattern);
		} catch (final Exception e) {
			return null;
		}
	}

	public static Pattern defaultInclude() {
		try {
			final String pattern = //
			ConfigHelp.reference().getString("include-pattern");
			return Pattern.compile(pattern);
		} catch (final Exception e) {
			return null;
		}
	}

}
