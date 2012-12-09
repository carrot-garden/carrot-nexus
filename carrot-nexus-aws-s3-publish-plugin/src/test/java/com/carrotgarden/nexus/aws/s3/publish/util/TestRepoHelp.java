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
