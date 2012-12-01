/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.case_01;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonConfigProvider;

class AmazonConfigMock extends AmazonConfigProvider {

	AmazonConfigMock(final ApplicationConfiguration config) {
		this.config = config;
	}

}
