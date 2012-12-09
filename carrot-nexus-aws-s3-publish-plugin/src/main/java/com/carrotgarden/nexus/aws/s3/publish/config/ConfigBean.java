/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.util.Map;
import java.util.TreeMap;

/**
 * plug-in configuration properties bean
 * <p>
 * read only
 * <p>
 * see {@link Form}
 */
public class ConfigBean {

	private final Map<String, String> props;

	public ConfigBean(final Map<String, String> props) {
		this.props = props;
	}

	private boolean asBoolean(final String key) {
		return Boolean.parseBoolean((props.get(key)));
	}

	private int asInteger(final String key) {
		return Integer.parseInt((props.get(key)));
	}

	private String asString(final String key) {
		return props.get(key);
	}

	public String awsAccess() {
		return asString("aws-access");
	}

	public String awsSecret() {
		return asString("aws-secret");
	}

	public String bucket() {
		return asString("bucket");
	}

	public String prefix() {
		return asString("prefix");
	}

	public String comboId() {
		return asString("combo-id");
	}

	public String emailAddress() {
		return asString("email-address");
	}

	public String emailReports() {
		return asString("email-reports");
	}

	public boolean enableEmail() {
		return asBoolean("enable-email");
	}

	public boolean enableExclude() {
		return asBoolean("enable-exclude");
	}

	public boolean enableFeeds() {
		return asBoolean("enable-feeds");
	}

	public boolean enableScanner() {
		return asBoolean("enable-scanner");
	}

	public boolean enableStatus() {
		return asBoolean("enable-status");
	}

	public String endpoint() {
		return asString("endpoint");
	}

	public String excludePattern() {
		return asString("exclude-pattern");
	}

	public int healthPeriod() {
		return asInteger("health-period");
	}

	public String healthStrategy() {
		return asString("health-strategy");
	}

	public boolean publishReleases() {
		return asBoolean("publish-releases");
	}

	public boolean publishSnapshots() {
		return asBoolean("publish-snapshots");
	}

	public String scannerSchedule() {
		return asString("scanner-schedule");
	}

	@Override
	public String toString() {
		return new TreeMap<String, String>(props).toString();
	}

}
