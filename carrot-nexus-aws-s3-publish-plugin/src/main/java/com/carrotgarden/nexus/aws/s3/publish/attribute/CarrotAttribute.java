/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.attribute;

public interface CarrotAttribute {

	String ATTR_IS_SAVED = "carrot.aws.s3.is-saved";
	String ATTR_SAVE_TIME = "carrot.aws.s3.save-time";

	boolean isSaved();

	long saveTime();

}
