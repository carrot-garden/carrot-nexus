/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityDescriptorSupport;

import com.carrotgarden.nexus.aws.s3.publish.field.FieldUtil;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * plug-in configuration form UI design
 * <p>
 * see ./src/main/resources/reference.conf
 */
@Singleton
@Named(ConfigBean.NAME)
public class ConfigDescriptor extends CapabilityDescriptorSupport implements
		CapabilityDescriptor {

	public static FormField[] capaFields() {
		return FieldUtil.fieldArray(formFieldBundle(), formFieldDefault());
	}

	public static String capaHelp() {
		return ConfigHelp.reference().getString("form-header.help-text");
	}

	public static String capaName() {
		return ConfigHelp.reference().getString("form-header.label");
	}

	public static CapabilityType capaType() {
		return ConfigBean.TYPE;
	}

	public static Config formFieldBundle() {
		return ConfigHelp.reference().getConfig("form-field-bundle");
	}

	public static Config formFieldDefault() {
		return ConfigHelp.reference().getConfig("form-field-default");
	}

	public static Map<String, String> propsDefault() {

		final Config root = ConfigHelp.reference();

		return propsFrom(root);

	}

	public static Map<String, String> propsDefaultWithOverride(
			final Map<String, String> source) {

		final Map<String, String> fallback = propsDefault();

		final Map<String, String> target = new HashMap<String, String>();

		for (final String key : fallback.keySet()) {

			final String valueSource = source.get(key);
			final String valueFallback = fallback.get(key);

			if (valueSource == null || valueSource.length() == 0) {
				target.put(key, valueFallback);
			} else {
				target.put(key, valueSource);
			}
		}

		return target;

	}

	public static Map<String, String> propsFrom(final Config root) {

		final Config config = root.getConfig("form-field-bundle");

		final Map<String, String> props = new HashMap<String, String>();

		final Set<String> keySet = config.root().keySet();

		for (final String configId : keySet) {

			final Config configField = config.getConfig(configId);

			final String configValue = configField.getString("default-value");

			ConfigBean.log.info("### {}={}", configId, configValue);

			props.put(configId, configValue);

		}

		return props;

	}

	public static Map<String, String> propsFrom(final File file) {

		final Config root = ConfigFactory.parseFile(file);

		return propsFrom(root);

	}

	public ConfigDescriptor() {

		super(capaType(), capaName(), capaHelp(), capaFields());

	}

}
