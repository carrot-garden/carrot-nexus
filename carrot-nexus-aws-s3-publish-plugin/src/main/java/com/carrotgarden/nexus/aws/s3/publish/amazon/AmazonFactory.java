/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.amazon;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;

public interface AmazonFactory {

	AmazonManager create(ConfigEntry entry);

}
