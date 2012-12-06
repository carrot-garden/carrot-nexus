/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;

/** public view of plug-in capabilities */
public interface ConfigEntry {

	/** UUID of capability */
	String configId();

	/** current capability life cycle state */
	ConfigState configState();

	/** amazon provider serving this capability */
	AmazonService amazonService();

	/** repository id : all-id ('*') or group-id or repo-id */
	String comboId();

	/** should exclude repo path from publication? */
	boolean isExcluded(String path);

}
