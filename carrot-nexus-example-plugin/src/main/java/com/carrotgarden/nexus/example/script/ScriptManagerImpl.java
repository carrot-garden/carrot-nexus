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
package com.carrotgarden.nexus.example.script;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.plexus.appevents.Event;

@Component(role = ScriptManager.class, instantiationStrategy = "singleton")
public class ScriptManagerImpl extends AbstractLoggingComponent implements
		ScriptManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Requirement
	private ScriptStorage storage;

	@Requirement
	private PlexusContainer plexus;

	@Override
	public void actUponEvent(final Event<?> evt) {
		final Class<? extends Event<?>> c = (Class<? extends Event<?>>) evt
				.getClass();
		final String script = storage.getScript(c);

		if (script == null) {
			return;
		}

	}

}
