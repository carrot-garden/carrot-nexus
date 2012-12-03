/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityDescriptorSupport;

import com.carrotgarden.nexus.aws.s3.publish.util.Util;
import com.typesafe.config.Config;

/**
 * config gui design
 */
@Singleton
@Named(ConfigBean.NAME)
public class ConfigDescriptor extends CapabilityDescriptorSupport implements
		CapabilityDescriptor {

	protected final static Logger log = LoggerFactory
			.getLogger(ConfigDescriptor.class);

	public static FormField[] capaFields() {

		final List<FormField> fieldList = new LinkedList<FormField>();

		final Config config = Util.reference().getConfig("form-field");

		final Set<String> keySet = config.root().keySet();

		final TreeSet<String> treeSet = new TreeSet<String>(keySet);

		for (final String configId : treeSet) {

			final Config configField = config.getConfig(configId);

			final FormField formField = makeField(configId, configField);

			fieldList.add(formField);

		}

		return fieldList.toArray(new FormField[0]);

	}

	public static String capaHelp() {
		return Util.reference().getString("form-name.help-text");
	}

	public static String capaName() {
		return Util.reference().getString("form-name.label");
	}

	public static FormField makeField(final String configId,
			final Config configField) {

		try {

			final String type = configField.getString("type");
			final String label = configField.getString("label");
			final String helpLink = configField.getString("help-link");
			final String helpText = configField.getString("help-text");

			final String helpHref = "<a href='" + helpLink + "'>help-link</a>";

			final String help = helpHref + "<br>" + helpText;

			final ClassLoader loader = ConfigDescriptor.class.getClassLoader();

			@SuppressWarnings("unchecked")
			final Class<FormField> klaz = (Class<FormField>) Class.forName(
					type, true, loader);

			final Constructor<FormField> init = klaz.getDeclaredConstructor(
					String.class, // id
					String.class, // label
					String.class, // help
					boolean.class // required
					);

			final FormField formField = init.newInstance( //
					configId, // id
					label, // label
					help, // help
					true // required
					);

			return formField;

		} catch (final Exception e) {

			log.error("", e);

			return new StringTextFormField("invalid-id", "Ivalid Field",
					configField.toString(), true);
		}

	}

	public ConfigDescriptor() {

		super(ConfigBean.TYPE, capaName(), capaHelp(), capaFields());

	}

}
