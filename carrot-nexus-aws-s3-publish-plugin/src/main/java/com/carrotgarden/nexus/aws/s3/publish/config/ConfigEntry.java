/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.util.List;
import java.util.Set;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.mailer.Report;

/** public view of plug-in capabilities */
public interface ConfigEntry {

	/** UUID of capability */
	String configId();

	/** current capability life cycle state */
	ConfigState configState();

	/** compare against state */
	boolean isConfigState(ConfigState state);

	/** amazon provider serving this capability */
	AmazonService amazonService();

	/** repository id : '*' or group-id or repo-id */
	String comboId();

	/** should exclude repo path from publication? */
	boolean isExcluded(String path);

	/** is given report included in subscriptions? */
	boolean isSubscribed(Report report);

	/** report recipient list */
	List<String> reportEmailList();

	/** report subscription list */
	Set<Report> reportSubscribeSet();

}
