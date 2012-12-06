/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.util;

import java.io.File;

/**
 */
public class PathHelp {

	/** artifact path from root/file relation */
	public static String relativePath(final File root, final File file) {

		return root.toURI().relativize(file.toURI()).getPath();

	}

	/** nexus likes "/" path prefix */
	public static String rootFullPath(final String path) {

		return path.startsWith("/") ? path : "/" + path;

	}

	/** amazon hates "/" path prefix */
	public static String rootLessPath(final String path) {

		return path.startsWith("/") ? path.substring(1) : path;

	}

}
