/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.task;

import static com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttribute.*;
import static com.carrotgarden.nexus.aws.s3.publish.util.PathHelp.*;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

import com.carrotgarden.nexus.aws.s3.publish.scanner.CarrotListener;
import com.carrotgarden.nexus.aws.s3.publish.scanner.CarrotListenerSupport;
import com.carrotgarden.nexus.aws.s3.publish.scanner.CarrotScanner;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.RepoHelp;
import com.google.common.base.Throwables;

/**
 * attribute cleanup task
 */
@Named(CleanerTask.NAME)
public class CleanerTask extends BaseTask {

	public static final String NAME = "CleanerTask";

	public static final String KEY_COMBO_ID = "combo-id";

	public static String taskNameRule() {
		return NAME + " " + "(" + ConfigHelp.pluginName() + ")";
	}

	private final RepositoryRegistry repoRegistry;
	private final CarrotScanner scanner;

	@Inject
	public CleanerTask( //
			@Named("serial") final CarrotScanner scanner, //
			final RepositoryRegistry repoRegistry //

	) {

		this.scanner = scanner;
		this.repoRegistry = repoRegistry;

	}

	public String comboId() {
		return getParameter(KEY_COMBO_ID);
	}

	@Override
	protected Object doRun() throws Exception {

		final Pattern defaultExclude = ConfigHelp.defaultExclude();

		final List<String> repoList = RepoHelp
				.repoList(repoRegistry, comboId());

		for (final String repoId : repoList) {

			checkInterruption();

			final Repository repo = repoRegistry.getRepository(repoId);

			final AttributeStorage attributeStorage = repo
					.getAttributesHandler().getAttributeStorage();

			final File root = RepoHelp.repoRoot(repo);

			final CarrotListener listener = new CarrotListenerSupport() {

				private int countCleared;
				private int countScanned;
				private final int reportBatch = 1000;

				@Override
				public void onBegin() {

					log.info("##########################################");
					log.info("repo clean init : {}", repoId);

				}

				@Override
				public void onEnd() {

					log.info("repo stats : scanned={} cleared={}",
							countScanned, countCleared);
					log.info("repo clean done : {}", repoId);
					log.info("##########################################");
				}

				@Override
				public void onFile(final File file) {
					try {

						checkInterruption();

						countScanned++;

						if (countScanned % reportBatch == 0) {
							log.info("scanned={} cleared={}", //
									countScanned, countCleared);
						}

						final String path = //
						rootFullPath(relativePath(root, file));

						final ResourceStoreRequest request = //
						new ResourceStoreRequest(path);

						request.getRequestContext().put( //
								AccessManager.REQUEST_AUTHORIZED, "true");

						final StorageItem any = repo.retrieveItem(request);

						final boolean isFile = any instanceof StorageFileItem;

						if (!isFile) {
							return;
						}

						final StorageFileItem item = (StorageFileItem) any;

						final RepositoryItemUid uid = item
								.getRepositoryItemUid();

						final Attributes attributes = attributeStorage
								.getAttributes(uid);

						if (attributes.containsKey(ATTR_IS_SAVED)) {

							attributes.remove(ATTR_IS_SAVED);
							attributes.remove(ATTR_SAVE_TIME);

							attributeStorage.putAttributes(uid, attributes);

							countCleared++;

						}

					} catch (final Exception e) {

						Throwables.propagate(e);

					}

				}

				@Override
				public boolean skipDirectory(final File directory) {

					final String path = //
					rootFullPath(relativePath(root, directory));

					final boolean isExcluded = //
					defaultExclude.matcher(path).matches();

					return isExcluded;

				}

			};

			scanner.scan(root, listener);

		}

		return null;

	}

	@Override
	protected String getAction() {
		return "clean";
	}

	@Override
	protected String getMessage() {
		return getName();
	}

}
