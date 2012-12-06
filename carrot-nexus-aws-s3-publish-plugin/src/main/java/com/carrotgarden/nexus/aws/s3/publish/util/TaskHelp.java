/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.util;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

/**
 */
public class TaskHelp {

	/**
	 * find nexus task reference from user task
	 */
	public static ScheduledTask<?> reference(
			final NexusScheduler taskScheduler, final AbstractNexusTask<?> task) {

		final Map<String, List<ScheduledTask<?>>> map = taskScheduler
				.getAllTasks();

		if (map == null) {
			return null;
		}

		final String taskType = task.getClass().getSimpleName();

		final List<ScheduledTask<?>> referenceList = map.get(taskType);

		if (referenceList == null) {
			return null;
		}

		for (final ScheduledTask<?> reference : referenceList) {
			if (reference.getTask() == task) {
				return reference;
			}
		}

		return null;

	}

}
