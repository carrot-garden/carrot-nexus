package com.carrotgarden.nexus.aws.s3.publish.mailer;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;

public class TestReport {

	@Test
	public void testName() throws Exception {

		final String nameList = ConfigHelp.reference().getString(
				"form-field-bundle.email-reports.default-value");

		final Set<Report> reportList = Report.reportSet(nameList);

		assertEquals("enum vs reference.conf", reportList.size(),
				Report.values().length - 1);

	}

}
