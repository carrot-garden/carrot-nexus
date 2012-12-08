/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepoComboFormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.formfields.TextAreaFormField;

/**
 * form field builder
 */
public enum Field {

	STRING("string") {
		@Override
		public FormField newFromField(final String id, final String label,
				final String help, final boolean required, final String regex) {
			return new StringTextFormField(id, label, help, required, regex);
		}
	},
	TEXT_AREA("text-area") {
		@Override
		public FormField newFromField(final String id, final String label,
				final String help, final boolean required, final String regex) {
			return new TextAreaFormField(id, label, help, required, regex);
		}
	},
	NUMBER("number") {
		@Override
		public FormField newFromField(final String id, final String label,
				final String help, final boolean required, final String regex) {
			return new NumberTextFormField(id, label, help, required, regex);
		}
	}, //
	CHECKBOX("checkbox") {
		@Override
		public FormField newFromField(final String id, final String label,
				final String help, final boolean required, final String regex) {
			return new CheckboxFormField(id, label, help, required);
		}
	}, //
	REPO("repo") {
		@Override
		public FormField newFromField(final String id, final String label,
				final String help, final boolean required, final String regex) {
			return new RepoComboFormField(id, label, help, required, regex);
		}
	}, //
	REPO_OR_GROUP("repo-or-group") {
		@Override
		public FormField newFromField(final String id, final String label,
				final String help, final boolean required, final String regex) {
			return new RepoOrGroupComboFormField(id, label, help, required,
					regex);
		}
	}, //

	;

	private static final Logger log = LoggerFactory.getLogger(Field.class);

	public abstract FormField newFromField( //
			final String id, //
			final String label, //
			final String help, //
			final boolean required, //
			final String regex //
	);

	/**
	 * {@link FormField#getType() }
	 * <p>
	 * see reference.conf
	 */
	public final String type;

	Field(final String type) {
		this.type = type;
	}

	public static Field from(final String type) {

		for (final Field known : Field.values()) {
			if (known.type.equalsIgnoreCase(type)) {
				return known;
			}
		}

		log.error("wrong field type", new Exception("" + type));

		return STRING;

	}

}
