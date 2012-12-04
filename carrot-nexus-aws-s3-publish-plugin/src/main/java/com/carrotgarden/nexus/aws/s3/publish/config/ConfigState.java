/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

/** capability life cycle states; mutually exclusive */
public enum ConfigState {

	ADDED, //

	ENABLED, //

	ACTIVATED, //

	PASSIVATED, //

	DISABLED, //

	REMOVED, //

	;

	/** map from capability state into amazon store action */
	public ConfigAction action() {

		switch (this) {

		case ADDED:
			return ConfigAction.SKIP;

		case ENABLED:
			return ConfigAction.FAIL;

		case ACTIVATED:
			return ConfigAction.WORK;

		case PASSIVATED:
			return ConfigAction.FAIL;

		case DISABLED:
			return ConfigAction.SKIP;

		case REMOVED:
			return ConfigAction.SKIP;

		default:
			return ConfigAction.FAIL;

		}

	}

}
