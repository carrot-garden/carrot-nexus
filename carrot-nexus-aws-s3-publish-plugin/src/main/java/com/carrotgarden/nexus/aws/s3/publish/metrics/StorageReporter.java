/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.metrics;

import javax.inject.Inject;
import javax.inject.Named;

import com.carrotgarden.nexus.aws.s3.publish.storage.CarrotRepositoryStorage;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * {@link CarrotRepositoryStorage} reporter
 */
@Named("storage")
public class StorageReporter extends BaseReporter {

	public final Counter amazonIgnoredFileCount;
	public final Counter amazonPublishedFileCount;
	public final Counter amazonPublishedFileSize;
	public final Counter amazonRetriedFileCount;
	public final Counter amazonFailedFileCount;

	public final WatchQueueGuage repoFileWatch = new WatchQueueGuage();
	public final WatchQueueGuage skipFileWatch = new WatchQueueGuage();
	public final WatchQueueGuage saveFileWatch = new WatchQueueGuage();

	@Inject
	public StorageReporter( //
			@Named("reporter") final MetricsRegistry registry //
	) {

		super(registry);

		//

		amazonPublishedFileSize = newCounter("amazon published file size");
		amazonPublishedFileCount = newCounter("amazon published file count");
		amazonIgnoredFileCount = newCounter("amazon ingnored file count");
		amazonRetriedFileCount = newCounter("amazon retried file count");
		amazonFailedFileCount = newCounter("amazon failed file count");

		newGauge("file watch: discovered", repoFileWatch);
		newGauge("file watch: published", saveFileWatch);
		newGauge("file watch: ignored", skipFileWatch);

	}

}
