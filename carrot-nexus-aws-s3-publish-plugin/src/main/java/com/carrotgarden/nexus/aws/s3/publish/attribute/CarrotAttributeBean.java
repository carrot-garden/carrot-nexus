/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.attribute;

import java.io.File;
import java.util.Map;

import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;

public class CarrotAttributeBean implements CarrotAttribute {

	public static CarrotAttribute fromJson(final File file) throws Exception {

		final Map<String, String> props = ConfigHelp.jsonFrom(file);

		return new CarrotAttributeBean(props);

	}

	private final Map<String, String> props;

	public CarrotAttributeBean(final Map<String, String> props) {
		this.props = props;
	}

	@Override
	public boolean isSaved() {
		try {
			return Boolean.parseBoolean(props.get(ATTR_IS_SAVED));
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public long saveTime() {
		try {
			return Long.parseLong(props.get(ATTR_SAVE_TIME));
		} catch (final Exception e) {
			return 0;
		}
	}

}
