/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.case_01;

import temp.zAmazonConfig;
import temp.zAmazonServiceProvider;

class AmazonServiceMock extends zAmazonServiceProvider {

	AmazonServiceMock(final zAmazonConfig amazonConfig) {

		this.amazonConfig = amazonConfig;

	}
}
