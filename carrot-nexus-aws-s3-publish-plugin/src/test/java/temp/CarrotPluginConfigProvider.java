/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package temp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.logging.AbstractLoggingComponent;

@Component(role = CarrotPluginConfig.class)
public class CarrotPluginConfigProvider extends AbstractLoggingComponent
		implements CarrotPluginConfig {

	@Configuration(value = "${nexus-work}/conf/carrot-nexus-aws-s3-publish-plugin.xml")
	private File configFile;

	private BeanConfig configBean;

	private final ReentrantLock lock = new ReentrantLock();

	@Override
	public BeanKey getBeanKey(final String key) throws NoSuchKeyException {

		if (StringUtils.isEmpty(key)) {
			throw new NoSuchKeyException(key);
		}

		try {

			final BeanConfig c = getConfigBean();

			for (final BeanKey beanKey : c.getBeanKeys()) {
				if (key.equals(beanKey.getKey())) {
					return beanKey;
				}
			}

			throw new NoSuchKeyException(key);

		} catch (final Exception e) {
			throw new NoSuchKeyException(key);
		}
	}

	@Override
	public boolean isEnabled() {

		try {

			return getConfigBean().isEnabled();

		} catch (final ConfigurationException e) {

			getLogger().error("Unable to read configuration", e);

		} catch (final IOException e) {

			getLogger().error("Unable to read configuration", e);

		}

		return false;
	}

	@Override
	public void enable() throws ConfigurationException, IOException {

		getConfigBean().setEnabled(true);

		configFileSave();

	}

	@Override
	public void disable() throws ConfigurationException, IOException {

		getConfigBean().setEnabled(false);

		configFileSave();

	}

	protected BeanConfig getConfigBean() throws ConfigurationException,
			IOException {
		if (configBean != null) {
			return configBean;
		}

		lock.lock();

		Reader fr = null;

		FileInputStream is = null;

		try {
			is = new FileInputStream(configFile);

			// final NexusLvoPluginConfigurationXpp3Reader reader = new
			// NexusLvoPluginConfigurationXpp3Reader();

			fr = new InputStreamReader(is);

			configBean = null; // reader.read(fr);

		} catch (final FileNotFoundException e) {
			// This is ok, may not exist first time around
			if (!configFile.exists()) {
				FileUtils.copyURLToFile(
						getClass().getResource(
								"/META-INF/nexus-lvo-plugin/lvo-plugin.xml"),
						configFile);

				return getConfigBean();
			} else {
				throw e;
			}
		} catch (final IOException e) {
			getLogger().error(
					"IOException while retrieving configuration file", e);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (final IOException e) {
					// just closing if open
				}
			}

			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					// just closing if open
				}
			}

			lock.unlock();
		}

		return configBean;
	}

	protected void configFileSave() throws IOException {

		lock.lock();

		configFile.getParentFile().mkdirs();

		Writer fw = null;

		try {

			fw = new OutputStreamWriter(new FileOutputStream(configFile));

			// final NexusLvoPluginConfigurationXpp3Writer writer = new
			// NexusLvoPluginConfigurationXpp3Writer();

			// writer.write(fw, configuration);

		} finally {
			if (fw != null) {
				try {
					fw.flush();
					fw.close();
				} catch (final IOException e) {
					// just closing if open
				}
			}

			lock.unlock();
		}
	}

	protected void clearCache() {
		configBean = null;
	}

}
