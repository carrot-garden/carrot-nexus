/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.field;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.sonatype.nexus.formfields.FormField;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

/**
 * form field conventions; see reference.conf
 */
public class FieldUtil {

	public static final Comparator<Entry<String, ConfigValue>> //
	comparator = new Comparator<Entry<String, ConfigValue>>() {
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

	public static FormField[] fieldArray( //
			final Config fieldBundle, final Config fieldDefault) {

		final List<FormField> fieldList = new LinkedList<FormField>();

		final Set<Entry<String, ConfigValue>> entrySet = //
		new TreeSet<Entry<String, ConfigValue>>(FieldUtil.comparator);

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

	public static FormField fieldEntry(final String id, final Config config) {

		final String type = config.getString("type");
		final String label = config.getString("label");
		final String helpLink = config.getString("help-link");
		final String helpText = config.getString("help-text");
		final boolean required = config.getBoolean("required");
		final String regex = config.getString("valid-regex");

		final String help = help(helpLink, helpText);

		final FormField formField = FieldEnum.from(type).newFromField(//
				id, label, help, required, regex);

		return formField;

	}

	public static String help(final String helpLink, final String helpText) {
		final String helpHref = "<a href='" + helpLink + "'>help-link</a>";
		final String help = helpHref + "<br>" + helpText;
		return help;
	}

}
