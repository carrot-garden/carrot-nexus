/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import static org.sonatype.nexus.plugins.capabilities.CapabilityType.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.sonatype.nexus.plugins.capabilities.CapabilityType;

import com.carrotgarden.nexus.aws.s3.publish.util.Util;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * capability configuration properties bean
 * <p>
 * see ./src/main/resources/reference.conf
 */
public class ConfigBean {

	/** capability type id */
	public static final String NAME = "carrot.config.aws.s3.publish";

	public static final CapabilityType TYPE = capabilityType(NAME);

	public static Map<String, String> defaultProps() {

		final Config root = Util.reference();

		return propsFrom(root);

	}

	public static Map<String, String> propsFrom(final File file) {

		final Config root = ConfigFactory.parseFile(file);

		return propsFrom(root);

	}

	public static Map<String, String> propsFrom(final Config root) {

		final Config config = root.getConfig("form-field");

		final Map<String, String> props = new HashMap<String, String>();

		final Set<String> keySet = config.root().keySet();

		for (final String configId : keySet) {

			final Config configField = config.getConfig(configId);

			final String configValue = configField.getString("value");

			props.put(configId, configValue);

		}

		return props;

	}

	private final Map<String, String> props;

	public ConfigBean(final Map<String, String> props) {
		this.props = props;
	}

	public String awsAccess() {
		return props.get("aws-access");
	}

	public String awsSecret() {
		return props.get("aws-secret");
	}

	public String bucket() {
		return props.get("bucket");
	}

	public boolean enableEmail() {
		return Boolean.parseBoolean((props.get("enable-email")));
	}

	public boolean enableFeeds() {
		return Boolean.parseBoolean((props.get("enable-feeds")));
	}

	public boolean publishReleases() {
		return Boolean.parseBoolean((props.get("publish-releases")));
	}

	public boolean publishSnapshots() {
		return Boolean.parseBoolean((props.get("publish-snapshots")));
	}

	public String endpoint() {
		return props.get("endpoint");
	}

	public int healthPeriod() {
		return Integer.parseInt(props.get("health-period"));
	}

	public String repoId() {
		return props.get("repo-id");
	}

	@Override
	public String toString() {
		return new TreeMap<String, String>(props).toString();
	}

}
