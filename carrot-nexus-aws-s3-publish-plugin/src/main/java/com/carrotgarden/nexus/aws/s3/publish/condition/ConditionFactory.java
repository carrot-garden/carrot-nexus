/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.condition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.eventbus.EventBus;

@Named
@Singleton
public class ConditionFactory {

	@Inject
	private EventBus eventBus;

	public ManagedCondition managed(final String reason) {

		return new ManagedCondition(eventBus, reason);

	}

}
