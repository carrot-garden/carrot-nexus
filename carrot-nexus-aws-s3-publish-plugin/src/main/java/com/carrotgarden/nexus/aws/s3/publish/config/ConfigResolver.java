/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.carrotgarden.nexus.aws.s3.publish.util.RepoHelp;
import com.google.common.eventbus.Subscribe;

/**
 * provides exploded capability list per repository
 */
@Named
@Singleton
public class ConfigResolver {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ConfigEntryStore entryStore = new ConfigEntryStore();

	private final RepositoryRegistry registry;
	private final EventBus eventBus;

	@Inject
	public ConfigResolver( //
			final RepositoryRegistry registry, //
			final EventBus eventBus //
	) {

		this.registry = registry;
		this.eventBus = eventBus;

		this.eventBus.register(this);

	}

	/** involve entry into repository */
	@Subscribe
	public void handle(final ConfigEntry configEntry) {

		/** individual or group or virtual id */
		final String comboId = configEntry.comboId();

		final List<String> repoList = RepoHelp.repoList(registry, comboId);

		for (final String repoId : repoList) {

			final ConfigEntryMap entryMap = entryMap(repoId);

			entryMap.put(configEntry.configId(), configEntry);

		}

	}

	public ConfigEntryMap entryMap(final String repoId) {

		ConfigEntryMap map = entryStore.get(repoId);

		if (map == null) {
			map = new ConfigEntryMap();
			entryStore.putIfAbsent(repoId, map);
			map = entryStore.get(repoId);
		}

		return map;

	}

	/**
	 * find repository servicing entries
	 * 
	 * @param repoId
	 *            individual repository (not group, not virtual)
	 */
	public ConfigEntryList entryList(final String repoId) {

		final ConfigEntryMap map = entryMap(repoId);

		final ConfigEntryList list = new ConfigEntryList();

		list.addAll(map.values());

		return list;

	}

}
