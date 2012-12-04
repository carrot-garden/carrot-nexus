/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.case_01;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

import temp.zAmazonConfigProvider;


class AmazonConfigMock extends zAmazonConfigProvider {

	AmazonConfigMock(final ApplicationConfiguration config) {
		this.config = config;
	}

}
