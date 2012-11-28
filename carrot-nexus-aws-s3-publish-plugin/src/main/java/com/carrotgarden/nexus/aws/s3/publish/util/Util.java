/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.util;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotFile;

public class Util {

	public static String filePath(final ApplicationConfiguration config,
			final Repository repository, final File file) throws Exception {

		final File root = repoRoot(config, repository);

		return filePath(root, file);

	}

	/** nexus artifact path from root/file relation */
	public static String filePath(final File root, final File file) {

		final String path = root.toURI().relativize(file.toURI()).getPath();

		return path.startsWith("/") ? path : "/" + path;

	}

	public static Map<String, String> fromJson(final File file)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		final Map<String, String> map = mapper.readValue(file, Map.class);

		return map;

	}

	/** nexus path patterns that should not be sent to s3 */
	public static boolean isIgnoredPath(final String path) {
		if (path.startsWith("/.")) {
			return true;
		}
		if (path.startsWith("/archetype-catalog.xml")) {
			return true;
		}
		if (path.startsWith("/repository-metadata.xml")) {
			return true;
		}
		return false;
	}

	public static boolean isProperRepository(final Repository repository) {
		if (repository instanceof AbstractMavenRepository) {
			return true;
		}
		return false;
	}

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

	public static boolean processStorageFileItem(
			final AmazonService amazonService, final StorageFileItem item,
			final File file, final Logger log) {

		final String path = item.getPath();

		if (isIgnoredPath(path)) {
			return false;
		}

		log.info("path={} file={}", path, file);

		final Attributes attributes = item.getRepositoryItemAttributes();

		final boolean isSaved = amazonService.save(path, file);

		if (isSaved) {

			attributes.put(CarrotFile.ATTR_IS_SAVED, Boolean.TRUE.toString());

			attributes.put(CarrotFile.ATTR_SAVE_TIME,
					"" + System.currentTimeMillis());

			log.info("save success : path={}", path);

		} else {

			log.error("save failure : path={}", path);

		}

		return isSaved;

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

	/** nexus likes "/" path prefix */
	public static String rootFullPath(final String path) {
		return path.startsWith("/") ? path : "/" + path;
	}

	/** amazon hates "/" path prefix */
	public static String rootLessPath(final String path) {
		return path.startsWith("/") ? path.substring(1) : path;
	}

}
