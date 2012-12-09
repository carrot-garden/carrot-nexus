/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.util;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

/**
 */
public class RepoHelp {

	/**
	 * internal nexus representation for "All Repositories" group
	 */
	private static final String[] //
	REPO_ALL_ID_ARRAY = new String[] { "*", "all", "all_repo" };

	public static URL localURL(final String url) throws Exception {

		final String[] knonwProtocol = { "file:" };

		if (url == null) {
			return null;
		}

		for (final String protocol : knonwProtocol) {
			if (url.startsWith(protocol)) {
				return new URL(url);
			}
		}

		return new URL("file:" + url);

	}

	/** local root of repo store */
	public static File repoRoot(final Repository repository) throws Exception {

		final ResourceStoreRequest request = new ResourceStoreRequest("/");

		final LocalRepositoryStorage storage = repository.getLocalStorage();

		@SuppressWarnings("deprecation")
		final URL repoURL = storage.getAbsoluteUrlFromBase(repository, request);

		final URI repoURI = repoURL.toURI();

		return new File(repoURI);

	}

	public static File repoRoot(final ApplicationConfiguration config,
			final Repository repository) throws Exception {

		final URL url = localURL(repository.getLocalUrl());

		final File root;

		if (url == null) {

			/** original */

			final String repoId = repository.getId();

			final File work = config.getWorkingDirectory();

			root = new File(new File(work, "storage"), repoId);

		} else {

			/** customized */

			root = new File(url.toURI());

		}

		return root;

	}

	public static boolean isRepoAll(final String repoId) {
		for (final String repoAll : REPO_ALL_ID_ARRAY) {
			if (repoAll.equals(repoId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * list of repo id
	 * <p>
	 * from combo : "*" or group-id or repo-id
	 */
	public static List<String> repoList(final RepositoryRegistry registry,
			final String comboId) {

		final List<String> list = new LinkedList<String>();

		if (isRepoAll(comboId)) {
			final List<Repository> repoList = registry.getRepositories();
			for (final Repository repo : repoList) {
				if (repo instanceof GroupRepository) {
					continue;
				} else {
					list.add(repo.getId());
				}
			}
			return list;
		}

		final Repository combo;

		try {
			combo = registry.getRepository(comboId);
		} catch (final Exception e) {
			return list;
		}

		if (combo instanceof GroupRepository) {
			final GroupRepository group = (GroupRepository) combo;
			final List<Repository> repoList = group.getMemberRepositories();
			for (final Repository repo : repoList) {
				list.add(repo.getId());
			}
		} else {
			list.add(combo.getId());
		}

		return list;

	}

}
