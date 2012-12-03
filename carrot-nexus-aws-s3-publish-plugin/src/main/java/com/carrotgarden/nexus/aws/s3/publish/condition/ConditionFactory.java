package com.carrotgarden.nexus.aws.s3.publish.condition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEnabler;

@Named
@Singleton
public class ConditionFactory {

	@Inject
	private EventBus eventBus;

	public ManagedCondition managed(final String reason) {

		return new ManagedCondition(eventBus, reason);

	}

	public ReportingCondition reporting(final ConfigEnabler enabler,
			final boolean isOn) {

		return new ReportingCondition(eventBus, enabler, isOn);

	}

}
