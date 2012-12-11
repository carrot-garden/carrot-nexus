/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.Schedule;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;
import com.carrotgarden.nexus.aws.s3.publish.metrics.BaseReporter;
import com.carrotgarden.nexus.aws.s3.publish.metrics.Reporter;
import com.carrotgarden.nexus.aws.s3.publish.task.ScannerTask.ConfigType;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.yammer.metrics.core.Counter;

/**
 * enforce task create/destroy rules
 */
@Named
public class TaskManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final BaseReporter reporter;
	private final NexusScheduler scheduler;

	private final Counter metricsEnsureCount;

	@Inject
	public TaskManager( //
			final BaseReporter reporter, //
			final NexusScheduler scheduler //
	) {

		this.reporter = reporter;
		this.scheduler = scheduler;

		metricsEnsureCount = this.reporter
				.newCounter("number of times tasks have being configured");

	}

	public Reporter reporter() {
		return reporter;
	}

	private void cancel(final String configId, final ConfigType configType) {
		final ScheduledTask<?> reference = findReference(configId, configType);
		if (reference == null) {
			return;
		} else {
			/** do not interrupt */
			reference.cancel(false);
		}
	}

	public void cancelTasks(final String configId) {

		cancel(configId, ConfigType.ON_DEMAND);
		cancel(configId, ConfigType.SCHEDULED);

	}

	protected static CronSchedule scheduleDefault() {
		try {
			final String pattern = ConfigHelp.reference().getString(
					"form-field-bundle.scanner-schedule.default-value");
			return new CronSchedule(pattern);
		} catch (final Exception e) {
			return null;
		}
	}

	private void ensure(final String configId, final ConfigType configType,
			final ConfigBean configBean) {

		ScannerTask task = findTask(configId, configType);

		final boolean isNew;
		if (task == null) {
			isNew = true;
			task = newInstance(configId, configType);
		} else {
			isNew = false;
		}

		final ScheduledTask<?> reference;

		switch (configType) {

		case ON_DEMAND:

			reference = scheduler.submit(task.getName(), task);

			break;

		case SCHEDULED:

			final Schedule schedule = schedule(configBean.scannerSchedule());

			if (isNew) {
				reference = scheduler.schedule(task.getName(), task, schedule);
			} else {
				reference = findReference(configId, configType);
				reference.setSchedule(schedule);
				scheduler.updateSchedule(reference);
				reference.reset();
			}

			break;

		default:
			log.error("wrong type", new Exception("" + configType));
			return;
		}

		final Map<String, String> params = task.getParameters();

		/** see https://issues.sonatype.org/browse/NEXUS-5428 */
		params.put(NexusTask.ID_KEY, reference.getId());

		/** so nexus will send email on task failure */
		params.put(NexusTask.ALERT_EMAIL_KEY, configBean.emailAddress());

	}

	public void ensureTasks(final String configId, final ConfigBean configBean) {

		if (!configBean.enableScanner()) {
			return;
		}

		metricsEnsureCount.inc();

		cancel(configId, ConfigType.ON_DEMAND);
		cancel(configId, ConfigType.SCHEDULED);

		ensure(configId, ConfigType.ON_DEMAND, configBean);
		ensure(configId, ConfigType.SCHEDULED, configBean);

	}

	public void report(final StringBuilder text, final String configId) {

		reporter.report(text, "task manager");

		report(text, configId, ConfigType.ON_DEMAND);
		report(text, configId, ConfigType.SCHEDULED);

	}

	private void report(final StringBuilder text, final String configId,
			final ConfigType configType) {

		final ScannerTask task = findTask(configId, configType);

		if (task == null) {
			return;
		}

		final String title = "task config type : " + configType;

		task.reporter().report(text, title);

	}

	private ScheduledTask<?> findReference(final String configId,
			final ScannerTask.ConfigType configType) {

		final List<ScheduledTask<?>> referenceList = //
		scheduler.getAllTasks().get(ScannerTask.NAME);

		if (referenceList == null) {
			return null;
		}

		for (final ScheduledTask<?> reference : referenceList) {

			final ScannerTask scannerTask = (ScannerTask) reference.getTask();

			if (scannerTask.equals(configId, configType)) {
				return reference;
			}

		}

		return null;

	}

	private ScannerTask findTask(final String configId,
			final ScannerTask.ConfigType configType) {

		final ScheduledTask<?> reference = findReference(configId, configType);

		if (reference == null) {
			return null;
		}

		return (ScannerTask) reference.getTask();

	}

	private ScannerTask newInstance(final String configId,
			final ConfigType configType) {

		final ScannerTask task = scheduler
				.createTaskInstance(ScannerTask.class);

		final String name = ScannerTask.taskNameRule(configId, configType);
		task.getParameters().put(NexusTask.NAME_KEY, name);

		task.configId(configId);
		task.configType(configType);

		return task;
	}

	private CronSchedule schedule(final String pattern) {
		try {
			return new CronSchedule(pattern);
		} catch (final Exception e) {
			log.error("invalid schedule, using default", e);
			return scheduleDefault();
		}
	}

}
