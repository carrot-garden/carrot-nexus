package com.carrotgarden.nexus.example.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.TaskActivityDescriptor;

public class MainTask implements NexusTask {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public void addParameter(final String arg0, final String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean allowConcurrentExecution(final Map arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowConcurrentSubmission(final Map arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getParameter(final String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isExposed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldSendAlertEmail() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAlertEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TaskActivityDescriptor getTaskActivityDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

}
