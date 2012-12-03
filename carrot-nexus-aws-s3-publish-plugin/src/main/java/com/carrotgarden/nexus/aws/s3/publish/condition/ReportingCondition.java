/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.condition;

import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEnabler;

/**
 * expose bind/release events
 */
@Named(ReportingCondition.NAME)
public class ReportingCondition implements Condition {

	public static final String NAME = "carrot.condition.reporting";

	private final ConfigEnabler enalber;

	private final boolean isSatisfied;

	public ReportingCondition(final EventBus eventBus,
			final ConfigEnabler enalber, final boolean isSatisfied) {

		this.enalber = enalber;

		this.isSatisfied = isSatisfied;

	}

	@Override
	public boolean isSatisfied() {
		return isSatisfied;
	}

	@Override
	public Condition bind() {
		enalber.onEnable();
		return this;
	}

	@Override
	public Condition release() {
		enalber.onDisable();
		return this;
	}

	@Override
	public String explainSatisfied() {
		return "always satisfied";
	}

	@Override
	public String explainUnsatisfied() {
		return "always unsatisfied";
	}

}
