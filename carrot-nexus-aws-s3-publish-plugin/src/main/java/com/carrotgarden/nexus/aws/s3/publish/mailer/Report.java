/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.mailer;

import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;

/**
 * availabel mailer reports
 */
public enum Report {

	AMAZON_AVAILABLE("amazon-available"), //
	AMAZON_UNAVAILABLE("amazon-unavailable"), //
	AMAZON_HEALTH_REPORT("amazon-health-report"), //

	DEPLOY_SUCCESS("deploy-success"), //
	DEPLOY_FAILURE("deploy-failure"), //

	PLUGIN_ENABLED("plugin-enabled"), //
	PLUGIN_DISABLED("plugin-disabled"), //

	SCANNER_TASK_SUCCESS("scanner-task-success"), //
	SCANNER_TASK_FAILURE("scanner-task-failure"), //

	SCANNER_TASK_SUCCESS_REPORT("scanner-task-success-report"), //
	SCANNER_TASK_FAILURE_REPORT("scanner-task-failure-report"), //

	UNKNOWN("unknown"), //

	;

	private final static Logger log = LoggerFactory.getLogger(Report.class);

	public static Report from(final String name) {
		for (final Report known : values()) {
			if (known.name.equalsIgnoreCase(name.trim())) {
				return known;
			}
		}
		return UNKNOWN;
	}

	/**
	 * report name
	 * <p>
	 * see reference.conf
	 */
	public final String name;

	Report(final String name) {
		this.name = name;
	}

	public static Set<Report> reportSet(final String codeList) {

		final Set<Report> reportSet = EnumSet.noneOf(Report.class);

		if (codeList == null) {
			return reportSet;
		}

		final String separator = //
		ConfigHelp.reference().getString("text-area-list-separator");

		final String[] nameArray = codeList.split(separator);

		for (final String name : nameArray) {

			final Report report = from(name);

			if (report == UNKNOWN) {
				log.error("unknown report name : {}", name);
				continue;
			}

			reportSet.add(report);

		}

		return reportSet;

	}

}
