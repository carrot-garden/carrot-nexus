/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistryEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.carrotgarden.nexus.aws.s3.publish.util.CapaHelp;
import com.google.common.eventbus.Subscribe;

/**
 * provide default plug-in configuration on boot (only when missing)
 */
@Named
@Singleton
public class ConfigBoot implements EventBus.LoadOnStart {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Subscribe
	public void handle(final CapabilityRegistryEvent.AfterLoad event) {

		final CapabilityRegistry registry = event.getEventSender();

		try {

			final CapabilityType type = ConfigDescriptor.TYPE;

			if (CapaHelp.hasNoReference(registry, type)) {

				log.info("provide default capability type={}", type);

				final boolean isEnabled = false;

				final String notes = "default config";

				final Map<String, String> props = Form
						.propsDefault();

				registry.add(type, isEnabled, notes, props);

			}

		} catch (final Exception e) {
			throw new RuntimeException("default capability failure", e);
		}

	}

}
