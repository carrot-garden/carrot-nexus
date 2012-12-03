/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.amazon;

import org.sonatype.nexus.plugins.capabilities.Condition;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;

public interface AmazonManager {

	Condition condition();

	void config(ConfigBean config);

	void start();

	void stop();

}
