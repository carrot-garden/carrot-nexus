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

import com.carrotgarden.nexus.aws.s3.publish.util.Util;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class ConfigRegistry {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ConfigEntryStore entryStore = new ConfigEntryStore();

	private final RepositoryRegistry registry;
	private final EventBus eventBus;

	@Inject
	public ConfigRegistry( //
			final RepositoryRegistry registry, //
			final EventBus eventBus //
	) {

		this.registry = registry;
		this.eventBus = eventBus;

		eventBus.register(this);

		log.info("\n\t ### init");

	}

	@Subscribe
	public void handle(final ConfigEntry configEntry) {

		log.info("\n\t ### configEntry : {} / {}", configEntry.configId(),
				configEntry.configState());

		final String comboId = configEntry.comboId();

		final List<String> repoList = Util.repoList(registry, comboId);

		for (final String repoId : repoList) {

			log.info("\n\t ### comboId={} repoId={}", comboId, repoId);

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

	public ConfigEntryList entryList(final String repoId) {

		final ConfigEntryMap map = entryMap(repoId);

		final ConfigEntryList list = new ConfigEntryList();

		list.addAll(map.values());

		return list;

	}

	/** cumulative action from all providers */
	public ConfigAction action(final String repoId) {

		final ConfigEntryList entryList = entryList(repoId);

		if (entryList.isEmpty()) {
			return ConfigAction.SKIP;
		}

		int countFail = 0;
		int countWork = 0;
		int countSkip = 0;

		for (final ConfigEntry entry : entryList) {

			switch (entry.configState().action()) {
			case FAIL:
				countFail++;
				continue;
			case WORK:
				countWork++;
				continue;
			case SKIP:
				countSkip++;
				continue;
			}

		}

		if (countFail > 0) {
			return ConfigAction.FAIL;
		}

		if (countWork > 0) {
			return ConfigAction.WORK;
		}

		if (countSkip > 0) {
			return ConfigAction.SKIP;
		}

		throw new RuntimeException("logic error");

	}

}
