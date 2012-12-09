/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.scheduling.AbstractNexusTask;

public abstract class BaseTask extends AbstractNexusTask<Object> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** nexus task name/type convention */
	{
		final Named anno = getClass().getAnnotation(Named.class);
		if (anno == null) {
			throw new IllegalStateException("@Named is missing");
		}
		if (!anno.value().equals(getClass().getSimpleName())) {
			throw new IllegalStateException("@Named must match class name");
		}
	}

}
