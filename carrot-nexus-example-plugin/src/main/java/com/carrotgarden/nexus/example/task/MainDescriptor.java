package com.carrotgarden.nexus.example.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

public class MainDescriptor implements ScheduledTaskDescriptor {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
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
	public List<FormField> formFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isExposed() {
		// TODO Auto-generated method stub
		return false;
	}

}
