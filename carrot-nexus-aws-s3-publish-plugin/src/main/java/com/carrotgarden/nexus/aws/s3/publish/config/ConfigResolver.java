/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.config;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.carrotgarden.nexus.aws.s3.publish.util.CapaHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.RepoHelp;
import com.google.common.eventbus.Subscribe;

/**
 * provides exploded capability list per repository, resolved from repository
 * group membership
 */
@Named
@Singleton
public class ConfigResolver {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ConfigEntryStore entryStore = new ConfigEntryStore();

	private final RepositoryRegistry registry;
	private final EventBus eventBus;
	private final CapabilityRegistry capaRegistry;

	@Inject
	public ConfigResolver( //
			final CapabilityRegistry capaRegistry, //
			final RepositoryRegistry registry, //
			final EventBus eventBus //
	) {

		this.capaRegistry = capaRegistry;
		this.registry = registry;
		this.eventBus = eventBus;

		this.eventBus.register(this);

	}

	/**
	 * involve entry into repository
	 */
	@Subscribe
	public synchronized void handle(final ConfigEntry configEntry) {

		log.info("\n\t ### involve {} {}", //
				configEntry.configId(), configEntry.configState());

		switch (configEntry.configState()) {
		case ADDED:
			entryAdd(configEntry);
			break;
		case REMOVED:
			entryRemove(configEntry);
			break;
		default:
			return;
		}

	}

	/**
	 * change entry involvement with repository
	 */
	@Subscribe
	public synchronized void handle(
			final RepositoryGroupMembersChangedEvent event) {

		final String groupId = event.getGroupRepository().getId();

		log.info("\n\t ### change groupId : {}", groupId);

		final List<String> repoAdded = event.getAddedRepositoryIds();
		final List<String> repoRemoved = event.getRemovedRepositoryIds();

		final List<ConfigEntry> entryList = entryListForGroup(groupId);

		for (final ConfigEntry entry : entryList) {

			final String configId = entry.configId();

			for (final String repoId : repoAdded) {
				entryMap(repoId).put(configId, entry);
			}

			for (final String repoId : repoRemoved) {
				entryMap(repoId).remove(configId);
			}

		}

	}

	private List<ConfigEntry> entryListForGroup(final String groupId) {

		final List<ConfigEntry> entryList = new ArrayList<ConfigEntry>();

		final List<CapabilityReference> referenceList = CapaHelp.referenceList(
				capaRegistry, ConfigDescriptor.TYPE);

		for (final CapabilityReference reference : referenceList) {

			final ConfigEntry entry = reference
					.capabilityAs(ConfigCapability.class);

			if (entry.comboId().equals(groupId)) {
				entryList.add(entry);
			}

		}

		return entryList;

	}

	private void entryAdd(final ConfigEntry configEntry) {

		/** individual or group or virtual id */
		final String comboId = configEntry.comboId();

		final List<String> repoList = RepoHelp.repoList(registry, comboId);

		for (final String repoId : repoList) {

			final ConfigEntryMap entryMap = entryMap(repoId);

			entryMap.put(configEntry.configId(), configEntry);

		}

	}

	private void entryRemove(final ConfigEntry configEntry) {

		for (final ConfigEntryMap entryMap : entryStore.values()) {

			entryMap.remove(configEntry.configId());

		}

	}

	private ConfigEntryMap entryMap(final String repoId) {

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
