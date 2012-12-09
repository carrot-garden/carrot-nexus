/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.util;

import static com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttribute.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.metrics.SizeTimeRatio;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * single global amazon publication point
 */
public class AmazonHelp {

	private final static Counter metricsPublishFileCount = //
	Metrics.newCounter(AmazonHelp.class, //
			"global amazon publish file count");

	private final static Counter metricsAttribFailureCount = //
	Metrics.newCounter(AmazonHelp.class, //
			"global amazon attribute failure count");

	private final static Counter metricsPublishFailureCount = //
	Metrics.newCounter(AmazonHelp.class, //
			"global amazon publish failure count");

	private final static Counter metricsPublishFileSize = //
	Metrics.newCounter(AmazonHelp.class, //
			"global amazon publish file size");

	private final static Timer metricsPublishTransferTime = //
	Metrics.newTimer(AmazonHelp.class, //
			"global amazon transer time", //
			TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

	static {

		Metrics.newGauge(AmazonHelp.class,
				"global amazon transfer speed, byte/sec", //
				new SizeTimeRatio(//
						metricsPublishFileSize, metricsPublishTransferTime));

	}

	//

	/**
	 * store existing item to amazon
	 */
	public static boolean storeItem( //
			final AmazonService amazonService, //
			final Repository repository, //
			final StorageFileItem item, //
			final File file, //
			final Logger log //
	) {

		final String path = item.getPath();

		final TimerContext time = metricsPublishTransferTime.time();

		final boolean isSaved = amazonService.save(path, file);

		time.stop();

		if (isSaved) {

			metricsPublishFileCount.inc();
			metricsPublishFileSize.inc(file.length());

			try {

				final RepositoryItemUid uid = item.getRepositoryItemUid();

				final AttributeStorage attributeStorage = repository
						.getAttributesHandler().getAttributeStorage();

				final Attributes attributes = attributeStorage
						.getAttributes(uid);

				attributes.put(ATTR_IS_SAVED, "true");
				attributes.put(ATTR_SAVE_TIME, "" + System.currentTimeMillis());

				attributeStorage.putAttributes(uid, attributes);

			} catch (final Exception e) {

				metricsAttribFailureCount.inc();
				metricsPublishFailureCount.inc();

				final String message = //
				"attribute persist failure;" + //
						" repo=" + repository.getId() + //
						" path=" + path //
				;

				throw new RuntimeException(message, e);

			}

			return true;

		} else {

			metricsPublishFailureCount.inc();

			return false;

		}

	}

}
