/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.threads.NexusThreadFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.carrotgarden.nexus.aws.s3.publish.util.PathHelp;

//@Named(AmazonServiceProvider.NAME)
public class zAmazonServiceProvider implements zAmazonService {

	public static final String NAME = "carrot.amazon.service";

	@Inject
	protected zAmazonConfig amazonConfig;

	private final Runnable healtTask = new Runnable() {
		@Override
		public void run() {
			checkAvailable();
		}
	};

	private boolean isAvailable;

	private boolean isScheduled;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ScheduledExecutorService scheduler;

	{

		log.info("init " + NAME);

		final NexusThreadFactory factory = //
		new NexusThreadFactory("carrot", "amazon service");

		scheduler = Executors.newScheduledThreadPool(1, factory);

	}

	@Override
	public void checkAvailable() {

		try {

			final AmazonS3Client client = amazonConfig.client();

			final String bucket = amazonConfig.bucket();

			final GetBucketLocationRequest request = //
			new GetBucketLocationRequest(bucket);

			final String result = client.getBucketLocation(request);

			if (!setAvailable(true)) {
				log.info("amazon service available");
			}

		} catch (final Exception e) {

			if (setAvailable(false)) {
				log.error("amazon service unavailable");
			}

		}

	}

	@Override
	public boolean isAvailable() {

		if (!isScheduled) {

			checkAvailable();

			final long period = amazonConfig.healthPeriod();

			scheduler.scheduleAtFixedRate( //
					healtTask, period, period, TimeUnit.MILLISECONDS);

			isScheduled = true;

		}

		return isAvailable;

	}

	@Override
	public boolean kill(final String path) {

		if (!isAvailable()) {
			return false;
		}

		try {

			final AmazonS3Client client = amazonConfig.client();
			final String bucket = amazonConfig.bucket();

			final DeleteObjectRequest request = //
			new DeleteObjectRequest(bucket, PathHelp.rootLessPath(path));

			client.deleteObject(request);

			return true;

		} catch (final Exception e) {

			setAvailable(false);

			log.error("bada-boom", e);

			return false;

		}

	}

	@Override
	public boolean load(final String path, final File file) {

		if (!isAvailable()) {
			return false;
		}

		try {

			final AmazonS3Client client = amazonConfig.client();
			final String bucket = amazonConfig.bucket();

			final GetObjectRequest request = //
			new GetObjectRequest(bucket, PathHelp.rootLessPath(path));

			final ObjectMetadata result = client.getObject(request, file);

			return true;

		} catch (final Exception e) {

			setAvailable(false);

			log.error("bada-boom", e);

			return false;

		}

	}

	@Override
	public boolean save(final String path, final File file) {

		if (!isAvailable()) {
			return false;
		}

		try {

			final AmazonS3Client client = amazonConfig.client();
			final String bucket = amazonConfig.bucket();

			final PutObjectRequest request = //
			new PutObjectRequest(bucket, PathHelp.rootLessPath(path), file);

			final PutObjectResult result = client.putObject(request);

			return true;

		} catch (final Exception e) {

			setAvailable(false);

			log.error("bada-boom", e);

			return false;

		}

	}

	protected boolean setAvailable(final boolean next) {

		final boolean past = isAvailable;

		isAvailable = next;

		return past;

	}

}
