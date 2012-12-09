/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.condition.NexusIsActiveCondition;
import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonFactory;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonManager;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.condition.ConditionFactory;
import com.carrotgarden.nexus.aws.s3.publish.mailer.CarrotMailer;
import com.carrotgarden.nexus.aws.s3.publish.mailer.Report;
import com.carrotgarden.nexus.aws.s3.publish.metrics.Reporter;
import com.carrotgarden.nexus.aws.s3.publish.task.TaskManager;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.RepoHelp;

/**
 * plug-in configuration life cycle manager
 */
@Named(ConfigDescriptor.NAME)
public class ConfigCapability extends CapabilitySupport implements Capability,
		ConfigEntry {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final Reporter reporter;
	private final GavCalculator calculator;
	private final EventBus eventBus;
	private final CapabilityRegistry capaRegistry;
	private final RepositoryRegistry repoRegistry;
	private final TaskManager taskManager;
	private final NexusIsActiveCondition nexusCondition;
	private final AmazonFactory amazonFactory;
	private final AmazonManager amazonManager;
	private final CarrotMailer mailer;

	private volatile ConfigBean configBean;
	private volatile ConfigState configState;

	@Inject
	public ConfigCapability( //
			final CarrotMailer mailer, //
			final AmazonFactory amazonFactory, //
			@Named("base") final Reporter reporter, //
			@Named("maven2") final GavCalculator calculator, //
			final CapabilityRegistry capaRegistry, //
			final NexusIsActiveCondition nexusCondition, //
			final TaskManager scannerManager, //
			final RepositoryRegistry repoRegistry, //
			final EventBus eventBus, //
			final ConditionFactory conditionFactory //
	) {

		this.mailer = mailer;
		this.amazonFactory = amazonFactory;
		this.reporter = reporter;
		this.calculator = calculator;
		this.capaRegistry = capaRegistry;
		this.nexusCondition = nexusCondition;
		this.taskManager = scannerManager;
		this.repoRegistry = repoRegistry;
		this.eventBus = eventBus;

		amazonManager = amazonFactory.create(this);

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
	public boolean isConfigState(final ConfigState state) {
		return configState == state;
	}

	@Override
	public AmazonService amazonService() {
		return amazonManager;
	}

	@Override
	public String comboId() {
		return configBean.comboId();
	}

	private Pattern includePattern;
	private Pattern excludePattern;

	private void includePattern(final ConfigBean configBean) {

		includePattern = ConfigHelp.defaultInclude();

	}

	private void excludePattern(final ConfigBean configBean) {

		if (configBean.enableExclude()) {
			final String defaultPattern = ConfigHelp.defaultExclude().pattern();
			final String customPattern = configBean.excludePattern();
			final String resultPattern = defaultPattern + "|" + customPattern;
			excludePattern = excludeCustom(resultPattern);
		} else {
			excludePattern = ConfigHelp.defaultExclude();
		}

		log.debug("excludePattern : {}", excludePattern);

	}

	private Pattern excludeCustom(final String pattern) {
		try {
			return Pattern.compile(pattern);
		} catch (final Exception e) {
			log.error("invalid pattern, using default", e);
			return ConfigHelp.defaultExclude();
		}
	}

	private boolean isRelease(final String path) {
		return !isSnapshot(path);
	}

	private boolean isSnapshot(final String path) {
		return path.contains("-SNAPSHOT/");
	}

	/** FIXME think again */
	@Override
	public boolean isExcluded(final String path) {

		// if (!configBean.publishSnapshots() && isSnapshot(path)) {
		// return true;
		// }

		// if (!configBean.publishReleases() && isRelease(path)) {
		// return true;
		// }

		/** force exclude */
		if (excludePattern.matcher(path).matches()) {
			return true;
		}

		/** force include */
		if (includePattern.matcher(path).matches()) {
			return false;
		}

		/** permit only valid artifact */
		final Gav gav = calculator.pathToGav(path);
		if (gav == null) {
			return true;
		} else {
			return false;
		}

	}

	private Set<Report> reportSubscribeSet;

	@Override
	public Set<Report> reportSubscribeSet() {

		return reportSubscribeSet;

	}

	private void reportSubscribeSet(final String reportText) {

		reportSubscribeSet = Report.reportSet(reportText);

	}

	@Override
	public boolean isSubscribed(final Report report) {

		if (!configBean.enableEmail()) {
			return false;
		}

		return reportSubscribeSet.contains(report);

	}

	private List<String> reportEmailList;

	private void reportEmailList(final String addressText) {

		final String separator = //
		ConfigHelp.reference().getString("string-list-separator");

		final String[] addressArray = addressText.split(separator);

		reportEmailList = new ArrayList<String>();

		for (final String address : addressArray) {
			reportEmailList.add(address.trim());
		}

	}

	@Override
	public List<String> reportEmailList() {

		return reportEmailList;

	}

	//

	/** render config status page */
	@Override
	public String status() {

		final StringBuilder text = new StringBuilder(1024);
		text.append("<pre>");

		if (configBean.enableStatus()) {

			Reporter.DEFAULT.report(text, "global");

			amazonManager.reporter().report(text, "amazon provider");

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

		case ADDED:

			configBean = new ConfigBean(context().properties());

			includePattern(configBean);
			excludePattern(configBean);

			reportEmailList(configBean.emailAddress());
			reportSubscribeSet(configBean.emailReports());

			amazonManager.configure(configBean);

			break;

		case ENABLED:

			amazonManager.ensure();

			taskManager.ensureTasks(configId(), configBean);

			mailer.sendPluginReport(Report.PLUGIN_ENABLED, this);

			break;

		case DISABLED:

			mailer.sendPluginReport(Report.PLUGIN_DISABLED, this);

			taskManager.cancelTasks(configId());

			amazonManager.stop();

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
		configState(ConfigState.ENABLED);
	}

	@Override
	public void onPassivate() throws Exception {
		configState(ConfigState.DISABLED);
	}

	private String repoName() {

		if (RepoHelp.isRepoAll(comboId())) {
			return "All Repositories";
		}

		final Repository repo = repo();

		if (repo == null) {
			return "<invalid>";
		} else {
			return repo.getName();
		}

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

		return "Publish [" + configId() + "] " + repoName();

	}

	@Override
	public Condition activationCondition() {
		return nexusCondition;
	}

}
