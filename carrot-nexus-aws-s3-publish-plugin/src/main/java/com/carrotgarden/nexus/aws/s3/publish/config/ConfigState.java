/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

/** config life cycle states */
public enum ConfigState {

	UNKNOWN, //

	//

	ADDED, //
	ENABLED, //
	ACTIVATED, //
	PASSIVATED, //
	DISABLED, //
	REMOVED, //

	;

	/** state to appropriate action mapping */
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
