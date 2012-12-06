/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.condition.NexusIsActiveCondition;
import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonProvider;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.condition.ConditionFactory;
import com.carrotgarden.nexus.aws.s3.publish.condition.ManagedCondition;
import com.carrotgarden.nexus.aws.s3.publish.metrics.Reporter;
import com.carrotgarden.nexus.aws.s3.publish.task.TaskManager;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.RepoHelp;

/**
 * plug-in configuration life cycle manager
 */
@Named(ConfigBean.NAME)
public class ConfigCapability extends CapabilitySupport implements Capability,
		ConfigEntry {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final Reporter reporter;
	private final GavCalculator calculator;
	private final EventBus eventBus;
	private final CapabilityRegistry capaRegistry;
	private final RepositoryRegistry repoRegistry;
	private final TaskManager taskManager;
	private final AmazonProvider amazonProvider;
	private final ManagedCondition conditionRepoAll;
	private final NexusIsActiveCondition nexusCondition;

	private volatile ConfigBean configBean;
	private volatile ConfigState configState;

	@Inject
	public ConfigCapability( //
			@Named("base") final Reporter reporter, //
			@Named("maven2") final GavCalculator calculator, //
			final CapabilityRegistry capaRegistry, //
			final NexusIsActiveCondition nexusCondition, //
			final AmazonProvider amazonProvider, //
			final TaskManager scannerManager, //
			final RepositoryRegistry repoRegistry, //
			final EventBus eventBus, //
			final ConditionFactory factory //
	) {

		this.reporter = reporter;
		this.calculator = calculator;
		this.capaRegistry = capaRegistry;
		this.nexusCondition = nexusCondition;
		this.amazonProvider = amazonProvider;
		this.taskManager = scannerManager;
		this.repoRegistry = repoRegistry;
		this.eventBus = eventBus;

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
		return configBean.comboId();
	}

	private Pattern excludePattern;

	@Override
	public boolean isExcluded(final String path) {

		/** pattern */

		if (configBean.enableExclude()) {
			if (excludePattern.matcher(path).matches()) {
				return true;
			}
		}

		/** GAV */

		final Gav gav = calculator.pathToGav(path);

		if (gav == null) {
			return true;
		}

		if (gav.isSnapshot()) {
			if (configBean.publishSnapshots()) {
				return false;
			}
		} else {
			if (configBean.publishReleases()) {
				return false;
			}
		}

		return true;

	}

	//

	private Pattern defaultPattern() {
		try {
			final String pattern = ConfigHelp.reference().getString(
					"form-field-bundle.exclude-pattern.default-value");
			return Pattern.compile(pattern);
		} catch (final Exception e) {
			log.error("should not happen", e);
			return null;
		}
	}

	private Pattern excludePattern(final String pattern) {
		try {
			return Pattern.compile(pattern);
		} catch (final Exception e) {
			log.error("invalid pattern, using default", e);
			return defaultPattern();
		}
	}

	/** render config status page */
	@Override
	public String status() {

		final StringBuilder text = new StringBuilder(1024);
		text.append("<pre>");

		if (configBean.enableStatus()) {

			Reporter.DEFAULT.report(text, "global");

			amazonProvider.reporter().report(text, "amazon provider");

			taskManager.report(text, configId());

		} else {

			text.append(ConfigHelp.reference().getString(
					"form-footer.help-text"));

		}

		text.append("</pre>");
		return text.toString();

	}

	/**
	 * process config state change events
	 * <p>
	 * UI "save" actions translate into {@link #onUpdate()}, which re-create
	 * {@link #configBean}
	 */
	private void configState(final ConfigState configState) {

		log.info("\n\t ### configState : {} {}", configState, context().id());

		this.configState = configState;

		switch (configState) {

		case INIT:

			/** hack to populate default properties */

			final String date = "[" + new Date() + "]";

			final CapabilityIdentity id = context().id();
			final boolean enabled = false;
			final String notes;
			if (context().notes() == null) {
				notes = date;
			} else {
				notes = context().notes() + " " + date;
			}

			final Map<String, String> properties = ConfigDescriptor
					.propsDefaultWithOverride(context().properties());

			configBean = new ConfigBean(properties);

			new Thread() {
				@Override
				public void run() {
					try {
						capaRegistry.update(id, enabled, notes, properties);
					} catch (final Exception e) {
						log.error("", e);
					}
				}
			}.start();

			break;

		case ADDED:

			configBean = new ConfigBean(context().properties());

			amazonProvider.config(configBean);

			excludePattern = excludePattern(configBean.excludePattern());

			conditionRepoAll.setSatisfied( //
					RepoHelp.isRepoAll(configBean.comboId()));

			break;

		case ENABLED:

			amazonProvider.ensure();

			taskManager.ensureTasks(configId(), configBean);

			break;

		case DISABLED:

			taskManager.cancelTasks(configId());

			amazonProvider.stop();

			break;

		case REMOVED:

			// NOOP

			break;

		}

		eventBus.post(this);

	}

	protected boolean isActive() {
		return context().isActive();
	}

	protected boolean isEnabled() {
		return context().isEnabled();
	}

	@Override
	public void onCreate() throws Exception {
		// configState(ConfigState.INIT);
		configState(ConfigState.ADDED);
	}

	@Override
	public void onLoad() throws Exception {
		configState(ConfigState.ADDED);
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
	public void onActivate() throws Exception {
		if (configState == ConfigState.INIT) {
			return; // stay in INIT
		}
		configState(ConfigState.ENABLED);
	}

	@Override
	public void onPassivate() throws Exception {
		if (configState == ConfigState.INIT) {
			return; // stay in INIT
		}
		configState(ConfigState.DISABLED);
	}

	private String repoName() {

		if ("*".equals(comboId())) {
			return "All Repositories";
		}

		final Repository repo = repo();

		if (repo == null) {
			return "<invalid>";
		}

		return repo().getName();

	}

	private Repository repo() {
		try {
			return repoRegistry.getRepository(comboId());
		} catch (final Exception e) {
			return null;
		}
	}

	@Override
	public String description() {

		return "Publish[" + configId() + "] " + repoName();

	}

	@Override
	public Condition activationCondition() {
		return nexusCondition;
	}

}
