/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import static org.sonatype.nexus.plugins.capabilities.CapabilityType.*;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;

/**
 * plug-in configuration properties bean
 * <p>
 * read only
 * <p>
 * see ./src/main/resources/reference.conf
 */
public class ConfigBean {

	static final Logger log = LoggerFactory.getLogger(ConfigBean.class);

	/** capability type id */
	public static final String NAME = "carrot.config.aws.s3.publish";

	public static final CapabilityType TYPE = capabilityType(NAME);

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

	public boolean enableStatus() {
		return asBoolean("enable-status");
	}

	/** "*" or group-id or repo-id */
	public String comboId() {
		return asString("combo-id");
	}

	public String scannerSchedule() {
		return asString("scanner-schedule");
	}

	@Override
	public String toString() {
		return new TreeMap<String, String>(props).toString();
	}

}
