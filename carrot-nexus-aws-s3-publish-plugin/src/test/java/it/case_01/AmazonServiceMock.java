/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.case_01;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonConfig;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonServiceProvider;

class AmazonServiceMock extends AmazonServiceProvider {

	AmazonServiceMock(final AmazonConfig amazonConfig) {

		this.amazonConfig = amazonConfig;

	}
}
