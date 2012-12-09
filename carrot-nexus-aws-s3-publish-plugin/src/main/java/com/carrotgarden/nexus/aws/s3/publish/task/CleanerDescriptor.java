/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;

@Named(CleanerTask.NAME)
public class CleanerDescriptor extends AbstractScheduledTaskDescriptor {

	private final RepoOrGroupComboFormField //
	comboId = new RepoOrGroupComboFormField( //
			CleanerTask.KEY_COMBO_ID, //
			"Repository/Group", //
			"Select repository or group to remove custom attributes from.", //
			FormField.MANDATORY //
	);

	@Override
	public String getId() {
		return CleanerTask.NAME;
	}

	@Override
	public String getName() {
		return CleanerTask.taskNameRule();
	}

	@Override
	public List<FormField> formFields() {
		final List<FormField> fields = new ArrayList<FormField>();

		fields.add(comboId);

		return fields;

	}

}
