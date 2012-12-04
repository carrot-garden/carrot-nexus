/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

//@Singleton
//@Named(AmazonConfigProvider.NAME)
public class zAmazonConfigProvider implements zAmazonConfig {

	public static final String NAME = "carrot.amazon.config";

	/** properties defaults resource inside plug-in jar */
	private static final String PROPS_INIT = "/" + PROPS_FILE;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	protected ApplicationConfiguration config;

	{

		log.info("init " + NAME);

	}

	private File configFile() {

		return new File(config.getConfigurationDirectory(), PROPS_FILE);

	}

	private Map<String, String> configMap;

	private long configFileLastLoaded;

	private boolean isUpdatePending() {
		return configMap == null
				|| configFileLastLoaded < configFile().lastModified();
	}

	private synchronized Map<String, String> configMap() {

		if (isUpdatePending()) {

			if (!configFile().exists()) {
				configMapSaveDefault();
			}

			configMap = configMapLoad(configFile());

			configFileLastLoaded = System.currentTimeMillis();

		}

		return configMap;

	}

	private Map<String, String> configMapLoad(final File file) {

		final Map<String, String> configMap = new HashMap<String, String>();

		final Properties properties = PropertyUtils.loadProperties(file);

		for (final Entry<?, ?> e : properties.entrySet()) {

			final String key = StringUtils.defaultString(e.getKey(), null);

			if (key != null) {
				configMap.put(key,
						StringUtils.defaultString(e.getValue(), null));
			}

		}

		return configMap;

	}

	private void configMapSaveDefault() {

		InputStream input = null;
		OutputStream output = null;

		try {

			input = zAmazonConfigProvider.class.getResourceAsStream(PROPS_INIT);
			output = new FileOutputStream(configFile());

			IOUtil.copy(input, output);

			log.info("made defaults file=" + configFile().getAbsolutePath());

		} catch (final Exception e) {

			log.warn("could not write configuration to path "
					+ configFile().getAbsolutePath(), e);

		} finally {
			IOUtil.close(input);
			IOUtil.close(output);
		}

	}

	private Boolean configBoolean(final String key) {
		return Boolean.valueOf(configMap().get(key));
	}

	private Long configLong(final String key) {
		return Long.parseLong(configMap().get(key));
	}

	private String configString(final String key) {
		return StringUtils.defaultString(configMap().get(key), null);
	}

	@Override
	public AWSCredentials credentials() {

		final String username = configString(KEY_CRED_USER);
		final String password = configString(KEY_CRED_PASS);

		return new BasicAWSCredentials(username, password);

	}

	@Override
	public String bucket() {
		return configString(KEY_BUCKET_NAME);
	}

	@Override
	public String endpoint() {
		return configString(KEY_AMAZON_ENDPOINT);
	}

	private AmazonS3Client client;

	@Override
	public AmazonS3Client client() {

		if (client == null || isUpdatePending()) {

			client = new AmazonS3Client(credentials());

			client.setEndpoint(endpoint());

		}

		return client;

	}

	@Override
	public long healthPeriod() {
		return configLong(KEY_HEALTH_PERIOD);
	}

}
