package com.carrotgarden.nexus.aws.s3.publish.task;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTaskManager {

	@Test
	public void testCron() throws Exception {

		assertNotNull("default cron schedule", TaskManager.scheduleDefault());

	}

}
