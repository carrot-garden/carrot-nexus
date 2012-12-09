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
