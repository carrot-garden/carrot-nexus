/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.scanner;

import java.io.File;

import org.sonatype.sisu.resource.scanner.Listener;

public interface CarrotListener extends Listener {

	boolean skipDirectory(File directory);

	boolean skipFile(File file);

}
