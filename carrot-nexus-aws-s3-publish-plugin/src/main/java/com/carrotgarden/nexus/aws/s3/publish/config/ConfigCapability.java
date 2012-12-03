/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.condition.SatisfiedCondition;
import org.sonatype.nexus.plugins.capabilities.internal.condition.UnsatisfiedCondition;
import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.support.condition.Conditions;
import org.sonatype.nexus.plugins.capabilities.support.condition.RepositoryConditions;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonProvider;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.condition.ConditionFactory;
import com.carrotgarden.nexus.aws.s3.publish.condition.ManagedCondition;
import com.carrotgarden.nexus.aws.s3.publish.condition.ReportingCondition;
import com.carrotgarden.nexus.aws.s3.publish.util.Util;

/**
 * config life cycle
 */
@Named(ConfigBean.NAME)
public class ConfigCapability extends CapabilitySupport implements Capability,
		ConfigEnabler, ConfigEntry {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private ConfigRegistry configRegistry;

	@Inject
	private EventBus eventBus;

	@Inject
	private Conditions conditions;

	@Inject
	private RepositoryRegistry registry;

	private final ReportingCondition conditionReporter;

	private final ManagedCondition conditionRepoAll;

	@Inject
	private AmazonProvider amazonProvider;

	private volatile ConfigBean configBean;

	private volatile ConfigState configState;

	@Inject
	public ConfigCapability(final ConditionFactory factory) {

		conditionReporter = factory.reporting(this, false);

		conditionRepoAll = factory.managed("repo-all");

	}

	//

	@Override
	public String configId() {
		return context().id().toString();
	}

	@Override
	public ConfigState configState() {
		return configState;
	}

	@Override
	public AmazonService amazonService() {
		return amazonProvider;
	}

	@Override
	public String comboId() {
		return configBean.repoId();
	}

	//

	private void configState(final ConfigState configState) {

		log.info("\n\t ### configState={}", configState);

		this.configState = configState;

		switch (configState) {

		case ADDED:

			configBean = new ConfigBean(context().properties());

			conditionRepoAll.setSatisfied( //
					Util.isRepoAll(configBean.repoId()));

			amazonProvider.config(configBean);

			break;

		case ENABLED:
			amazonProvider.start();
			break;

		case DISABLED:
			amazonProvider.stop();
			break;

		case REMOVED:
			break;

		}

		eventBus.post(this);

	}

	private boolean isEnabled() {
		return context().isEnabled();
	}

	@Override
	public void onCreate() throws Exception {
		configState(ConfigState.ADDED);
		if (isEnabled()) {
			configState(ConfigState.ENABLED);
		}
	}

	@Override
	public void onLoad() throws Exception {
		configState(ConfigState.ADDED);
		if (isEnabled()) {
			configState(ConfigState.ENABLED);
		}
	}

	@Override
	public void onUpdate() throws Exception {

		if (isEnabled()) {
			configState(ConfigState.DISABLED);
		}

		configState(ConfigState.REMOVED);

		configState(ConfigState.ADDED);

		if (isEnabled()) {
			configState(ConfigState.ENABLED);
		}

	}

	@Override
	public void onRemove() throws Exception {
		configState(ConfigState.REMOVED);
	}

	@Override
	public void onActivate() {
		configState(ConfigState.ACTIVATED);
	}

	@Override
	public void onPassivate() {
		configState(ConfigState.PASSIVATED);
	}

	@Override
	public void onEnable() {
		configState(ConfigState.ENABLED);
	}

	@Override
	public void onDisable() {
		configState(ConfigState.DISABLED);
	}

	private String repoName() {
		return repo() == null ? comboId() : repo().getName();
	}

	private Repository repo() {
		try {
			return registry.getRepository(comboId());
		} catch (final Exception e) {
			return null;
		}
	}

	/** activate/deactivate config; keep in registry */
	@Override
	public Condition activationCondition() {

		final Condition repoEnabled = conditions.repository()
				.repositoryIsInService(new RepositoryConditions.RepositoryId() {
					@Override
					public String get() {
						return comboId();
					}
				});

		final Condition repoExists = conditions.repository().repositoryExists(
				new RepositoryConditions.RepositoryId() {
					@Override
					public String get() {
						return comboId();
					}
				});

		final Condition notUpdate = conditions.capabilities()
				.passivateCapabilityDuringUpdate(context().id());

		final Condition storeExists = //
		conditions.logical()
				.or(conditionReporter, conditionRepoAll, repoExists);

		final Condition never = new UnsatisfiedCondition("never");

		return conditions.logical().and(amazonProvider.condition(),
				storeExists, notUpdate);

	}

	/** destroy config from registry when invalid */
	@Override
	public Condition validityCondition() {

		final Condition repoExists = conditions.repository().repositoryExists(
				new RepositoryConditions.RepositoryId() {
					@Override
					public String get() {
						return comboId();
					}
				});

		// return conditions.logical().or(conditionRepoAll, repoExists);

		return new SatisfiedCondition("always-valid");

	}

	@Override
	public String description() {

		return "Repository/Group : " + repoName();

	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof ConfigCapability) {
			final ConfigCapability that = (ConfigCapability) other;
			return that.configId().equals(this.configId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return configId().hashCode();
	}

}
