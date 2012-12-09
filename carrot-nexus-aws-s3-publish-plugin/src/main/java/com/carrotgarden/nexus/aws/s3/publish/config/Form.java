/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.sonatype.nexus.formfields.FormField;

import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

/**
 * plug-in capability configuration UI form design
 * <p>
 * see ./src/main/resources/reference.conf
 */
public class Form {

	/**
	 * order by field entry position in reference.conf
	 */
	public static final Comparator<Entry<String, ConfigValue>> //
	COMPARATOR = new Comparator<Entry<String, ConfigValue>>() {
		@Override
		public int compare( //
				final Entry<String, ConfigValue> o1, //
				final Entry<String, ConfigValue> o2 //
		) {
			final int n1 = o1.getValue().origin().lineNumber();
			final int n2 = o2.getValue().origin().lineNumber();
			return n1 == n2 ? 0 : (n1 > n2 ? 1 : -1);
		}
	};

	/**
	 * build ordered form field array
	 */
	public static FormField[] fieldArray( //
			final Config fieldBundle, final Config fieldDefault) {

		final List<FormField> fieldList = new LinkedList<FormField>();

		final Set<Entry<String, ConfigValue>> entrySet = //
		new TreeSet<Entry<String, ConfigValue>>(COMPARATOR);

		entrySet.addAll(fieldBundle.root().entrySet());

		for (final Entry<String, ConfigValue> entry : entrySet) {

			final String configId = entry.getKey();
			final Config configField = fieldBundle.getConfig(configId);

			final FormField formField = fieldEntry(configId,
					configField.withFallback(fieldDefault));

			fieldList.add(formField);

		}

		return fieldList.toArray(new FormField[0]);

	}

	/**
	 * form field attributes convention
	 */
	public static FormField fieldEntry(final String id, final Config config) {

		final String type = config.getString("type");
		final String label = config.getString("label");
		final String helpLink = config.getString("help-link");
		final String helpText = config.getString("help-text");
		final boolean required = config.getBoolean("required");
		final String regex = config.getString("valid-regex");

		final String help = fieldHelp(helpLink, helpText);

		final FormField formField = Field.from(type).newFromField(//
				id, label, help, required, regex);

		return formField;

	}

	/**
	 * tool tip text builder
	 */
	public static String fieldHelp(final String helpLink, final String helpText) {
		final String helpHref = "<a href='" + helpLink + "'>help-link</a>";
		final String help = helpHref + "<br>" + helpText;
		return help;
	}

	/**
	 * collection of all form fields
	 */
	public static Config formFieldBundle() {
		return ConfigHelp.reference().getConfig("form-field-bundle");
	}

	/**
	 * template/default/fallback field
	 */
	public static Config formFieldDefault() {
		return ConfigHelp.reference().getConfig("form-field-default");
	}

	public static FormField[] formFields() {
		return fieldArray(formFieldBundle(), formFieldDefault());
	}

	public static String formHelp() {
		return ConfigHelp.reference().getString("form-header.help-text");
	}

	public static String formName() {
		return ConfigHelp.reference().getString("form-header.label");
	}

	/**
	 * default form field properties
	 */
	public static Map<String, String> propsDefault() {

		final Config root = ConfigHelp.reference();

		return propsFrom(root);

	}

	/**
	 * use source property value if available, and default value as fallback
	 */
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

	/**
	 * form filed default value extract convention
	 */
	public static Map<String, String> propsFrom(final Config root) {

		final Config config = root.getConfig("form-field-bundle");

		final Map<String, String> props = new HashMap<String, String>();

		final Set<String> keySet = config.root().keySet();

		for (final String configId : keySet) {

			final Config configField = config.getConfig(configId);

			final String configValue = configField.getString("default-value");

			props.put(configId, configValue);

		}

		return props;

	}

	/**
	 * make properties from custom reference.conf file with fallback on defaults
	 */
	public static Map<String, String> propsFrom(final File file) {

		final Config root = ConfigFactory.parseFile(file)
				.withFallback(ConfigHelp.reference()).resolve();

		return propsFrom(root);

	}

}
