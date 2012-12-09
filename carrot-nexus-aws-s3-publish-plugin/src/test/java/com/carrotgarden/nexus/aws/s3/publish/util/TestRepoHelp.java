/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRepoHelp {

	@Test
	public void testRepoAll() throws Exception {

		assertTrue(RepoHelp.isRepoAll("*"));

		assertTrue(RepoHelp.isRepoAll("all"));

		assertTrue(RepoHelp.isRepoAll("all_repo"));

	}

}
