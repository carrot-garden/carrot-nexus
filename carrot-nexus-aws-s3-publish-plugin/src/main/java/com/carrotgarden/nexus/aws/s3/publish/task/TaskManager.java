/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.Schedule;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;
import com.carrotgarden.nexus.aws.s3.publish.task.ScannerTask.ConfigType;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

/**
 * enforce task create/destroy rules
 */
@Named
public class TaskManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final NexusScheduler taskScheduler;

	@Inject
	public TaskManager(//
			final NexusScheduler taskScheduler //
	) {

		this.taskScheduler = taskScheduler;

	}

	private void cancel(final String configId, final ConfigType configType) {
		final ScheduledTask<?> reference = findReference(configId, configType);
		if (reference == null) {
			return;
		} else {
			reference.cancel(true);
		}
	}

	public void cancelTasks(final String configId) {

		cancel(configId, ConfigType.ON_DEMAND);
		cancel(configId, ConfigType.SCHEDULED);

	}

	private String defaultPattern() {
		return ConfigHelp.reference().getString(
				"form-field-bundle.scanner-schedule.default-value");
	}

	private CronSchedule defaultSchedule() {
		try {
			return new CronSchedule(defaultPattern());
		} catch (final Exception e) {
			log.error("should not happen", e);
			return null;
		}
	}

	private void ensure(final String configId, final ConfigType configType,
			final ConfigBean configBean) {

		ScannerTask task = findTask(configId, configType);

		switch (configType) {

		case ON_DEMAND:

			if (task == null) {
				task = newInstance(configId, configType);
			}

			taskScheduler.submit(task.name(), task);

			break;

		case SCHEDULED:

			final Schedule schedule = schedule(configBean.scannerSchedule());

			if (task == null) {
				task = newInstance(configId, configType);
				taskScheduler.schedule(task.name(), task, schedule);
			} else {
				final ScheduledTask<?> reference = //
				findReference(configId, configType);
				reference.setSchedule(schedule);
				taskScheduler.updateSchedule(reference);
				reference.reset();
			}

			break;

		default:
			log.error("wrong type", new Exception("" + configType));
			return;
		}

	}

	private void sleep() {
		try {
			Thread.sleep(1 * 1000);
		} catch (final InterruptedException e) {
			//
		}
	}

	private final Counter metricsEnsureCount = Metrics.newCounter(getClass(),
			"number of times tasks have being configured");

	public void ensureTasks(final String configId, final ConfigBean configBean) {

		metricsEnsureCount.inc();

		cancel(configId, ConfigType.ON_DEMAND);
		cancel(configId, ConfigType.SCHEDULED);

		ensure(configId, ConfigType.ON_DEMAND, configBean);
		ensure(configId, ConfigType.SCHEDULED, configBean);

	}

	public void report(final StringBuilder text, final String configId) {

		report(text, configId, ConfigType.ON_DEMAND);
		report(text, configId, ConfigType.SCHEDULED);

	}

	private void report(final StringBuilder text, final String configId,
			final ConfigType configType) {

		final ScannerTask task = findTask(configId, configType);

		if (task == null) {
			return;
		}

		final String title = "task : " + configType;

		task.reporter().report(text, title);

	}

	private ScheduledTask<?> findReference(final String configId,
			final ScannerTask.ConfigType configType) {

		final List<ScheduledTask<?>> referenceList = //
		taskScheduler.getAllTasks().get(ScannerTask.NAME);

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

		final ScannerTask task = taskScheduler
				.createTaskInstance(ScannerTask.class);

		final String name = ScannerTask.taskNameRule(configId, configType);

		task.name(name);
		task.configId(configId);
		task.configType(configType);

		return task;
	}

	private CronSchedule schedule(final String pattern) {
		try {
			return new CronSchedule(pattern);
		} catch (final Exception e) {
			log.error("invalid schedule, using default", e);
			return defaultSchedule();
		}
	}

}
