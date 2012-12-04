/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;

/** public capability view */
public interface ConfigEntry {

	/** UUID of capability */
	String configId();

	/** current capability state */
	ConfigState configState();

	/** amazon provider tied to the capability */
	AmazonService amazonService();

	/** repository id : all-id ('*') or group-id or repo-id */
	String repoId();

}
