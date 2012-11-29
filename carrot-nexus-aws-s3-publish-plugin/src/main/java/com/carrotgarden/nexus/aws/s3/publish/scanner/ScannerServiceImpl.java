/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.scanner;

import static com.carrotgarden.nexus.aws.s3.publish.util.Util.*;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.threads.NexusThreadFactory;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotFile;

@Named
@Singleton
public class ScannerServiceImpl implements ScannerService {

	public static final String NAME = "ScannerServiceImpl";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private ApplicationConfiguration config;

	@Inject
	private AmazonService amazonService;

	private final ScheduledExecutorService scheduler;

	{

		log.info("init " + NAME);

		final NexusThreadFactory factory = //
		new NexusThreadFactory("carrot", "scanner service");

		scheduler = Executors.newScheduledThreadPool(1, factory);

	}

	private void process(final Repository repository) {

		final File root;
		try {
			root = repoRoot(config, repository);
			log.info("found root : " + root);
		} catch (final Exception e) {
			log.error("wrong repot root", e);
			return;
		}

		process(repository, root);

	}

	private void process(final Repository repository, final File root) {

		@SuppressWarnings("unchecked")
		final Iterator<File> iterator = FileUtils
				.iterateFiles(root, null, true);

		while (iterator.hasNext()) {

			final File file = iterator.next();

			final String path = rootFullPath(relativePath(root, file));

			if (isIgnoredPath(path)) {
				continue;
			}

			log.info("found path=" + path);

			final boolean isDone = process(repository, file, path);

			if (isDone) {
				continue;
			} else {
				sleep(1000);
			}

		}

	}

	private void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final Exception e) {
			//
		}
	}

	private boolean process(final Repository repository, final File file,
			final String path) {

		final ResourceStoreRequest request = new ResourceStoreRequest(path);

		final StorageItem item;
		try {
			item = repository.retrieveItem(request);
		} catch (final Exception e) {
			log.error("unexpected", e);
			return false;
		}

		final boolean isFile = item instanceof StorageFileItem;

		if (!isFile) {
			return false;
		}

		final Attributes attributes = item.getRepositoryItemAttributes();

		final String value = attributes.get(CarrotFile.ATTR_IS_SAVED);

		if ("true".equals(value)) {
			return true;
		}

		return processStorageFileItem(//
				amazonService, (StorageFileItem) item, file, log);

	}

	private final ConcurrentMap<String, Repository> repoMap = //
	new ConcurrentHashMap<String, Repository>();

	@Override
	public void register(final Repository repository) {

		final String id = repository.getId();

		if (isProperRepository(repository)) {

			if (repoMap.containsKey(id)) {
				log.info(" repo presnet: {}", id);
				return;
			}

			repoMap.put(id, repository);

			final ScannerTask scannerTask = new ScannerTask(repository);

			scheduler.scheduleAtFixedRate(scannerTask, 5, 5, TimeUnit.SECONDS);

			log.info("registered repo : {}", id);

		} else {

			log.error("wrong repo type : {}", id);

		}

	}

	private class ScannerTask implements Runnable {

		private final Repository repository;

		ScannerTask(final Repository repository) {
			this.repository = repository;
		}

		@Override
		public void run() {

			log.info("scan init repo= {}", repository.getId());

			process(repository);

			log.info("scan done repo= {}", repository.getId());

		}

	}

}
