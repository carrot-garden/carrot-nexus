/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import javax.inject.Named;

import com.carrotgarden.nexus.aws.s3.publish.task.ScannerTask.ConfigType;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;

/**
 * attribute cleanup task
 */
@Named(CleanerTask.NAME)
public class CleanerTask extends BaseTask {

	public static final String NAME = "CleanerTask";

	public static String taskNameRule(final String configId,
			final ConfigType configType) {
		return NAME + " [" + configId + "] " + configType + " ("
				+ ConfigHelp.reference().getString("plugin-name") + ")";
	}

	@Override
	protected Object doRun() throws Exception {
		return null;
	}

	@Override
	protected String getAction() {
		return "attribute";
	}

	@Override
	protected String getMessage() {
		return getName();
	}

}
