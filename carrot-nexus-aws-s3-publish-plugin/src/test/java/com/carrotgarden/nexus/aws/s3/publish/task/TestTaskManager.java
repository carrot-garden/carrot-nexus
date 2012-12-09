/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTaskManager {

	@Test
	public void testCron() throws Exception {

		assertNotNull("default cron schedule", TaskManager.scheduleDefault());

	}

}
