/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.condition;

import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.support.condition.ConditionSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

/**
 * expose {@link #setSatisfied(boolean)}
 */
@Named(ManagedCondition.NAME)
public class ManagedCondition extends ConditionSupport {

	public static final String NAME = "carrot.condition.managed";

	private final String reason;

	public ManagedCondition(final EventBus eventBus, final String reason) {
		super(eventBus);
		this.reason = reason;
	}

	@Override
	protected void doBind() {
		getEventBus().register(this);
	}

	@Override
	protected void doRelease() {
		getEventBus().unregister(this);
	}

	@Override
	public void setSatisfied(final boolean on) {
		super.setSatisfied(on);
	}

	@Override
	public String toString() {
		return reason;
	}

}
