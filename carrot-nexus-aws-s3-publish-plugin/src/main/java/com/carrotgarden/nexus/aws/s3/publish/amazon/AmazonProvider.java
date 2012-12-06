/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.amazon;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.threads.NexusThreadFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;
import com.carrotgarden.nexus.aws.s3.publish.metrics.AmazonReporter;
import com.carrotgarden.nexus.aws.s3.publish.metrics.Reporter;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.carrotgarden.nexus.aws.s3.publish.util.PathHelp;

@Named(AmazonProvider.NAME)
public class AmazonProvider implements AmazonService, AmazonManager {

	public static final String NAME = "carrot.amazon.provider";

	private static final NexusThreadFactory threadFactory = //
	new NexusThreadFactory("carrot", NAME);

	private static final ScheduledExecutorService scheduler = //
	Executors.newScheduledThreadPool(1, threadFactory);

	private final AmazonReporter reporter;

	@Inject
	public AmazonProvider( //
			final AmazonReporter reporter //
	) {

		this.reporter = reporter;

	}

	private long checkCount;

	private volatile AmazonS3Client client;

	private volatile ConfigBean config;

	private volatile ScheduledFuture<?> healthFuture;

	private final Runnable healtTask = new Runnable() {
		@Override
		public void run() {
			checkAvailable();
		}
	};

	private boolean isAvailable;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private long amazonThrottleExpiration() {
		return ConfigHelp.reference().getMilliseconds(
				"amazon-provider.throttle-expiration");
	}

	private boolean amazonThrottleExceptions() {
		return ConfigHelp.reference().getBoolean(
				"amazon-provider.throttle-exceptions");
	}

	// private final Cache<String, String> exceptionCache = CacheBuilder
	// .newBuilder()
	// .maximumSize(1000)
	// .expireAfterWrite(amazonThrottleExpiration(), TimeUnit.MILLISECONDS)
	// .build();

	private synchronized void checkAvailable() {

		reporter.requestCheckCount.inc();
		reporter.requestTotalCount.inc();

		try {

			final String bucket = config.bucket();

			final GetBucketLocationRequest request = //
			new GetBucketLocationRequest(bucket);

			final String result = client.getBucketLocation(request);

			if (!setAvailable(true) || isFirstScheck()) {
				log.info(NAME + " available");
			}

		} catch (final Exception e) {

			if (setAvailable(false) || isFirstScheck()) {
				log.error(NAME + " unavailable", e);
			}

		}

		checkCount++;

	}

	@Override
	public synchronized void config(final ConfigBean config) {
		this.config = config;
	}

	private AWSCredentials credentials() {

		final String username = config.awsAccess();
		final String password = config.awsSecret();

		return new BasicAWSCredentials(username, password);

	}

	@Override
	public boolean isAvailable() {
		return isAvailable;
	}

	private boolean isFirstScheck() {
		return checkCount == 0;
	}

	@Override
	public boolean kill(final String path) {

		reporter.requestKillCount.inc();
		reporter.requestTotalCount.inc();

		try {

			final String bucket = config.bucket();

			final DeleteObjectRequest request = //
			new DeleteObjectRequest(bucket, PathHelp.rootLessPath(path));

			client.deleteObject(request);

			setAvailable(true);

			return true;

		} catch (final Exception e) {

			processFailure(e);
			return false;

		}

	}

	@Override
	public boolean load(final String path, final File file) {

		reporter.requestLoadCount.inc();
		reporter.requestTotalCount.inc();

		try {

			final String bucket = config.bucket();

			final GetObjectRequest request = //
			new GetObjectRequest(bucket, PathHelp.rootLessPath(path));

			final ObjectMetadata result = client.getObject(request, file);

			setAvailable(true);

			return true;

		} catch (final AmazonS3Exception e) {

			switch (e.getStatusCode()) {
			case 404:
				log.error("path={} code={}", path, e.getErrorCode());
				break;
			default:
				processFailure(e);
				break;
			}
			return false;

		} catch (final Exception e) {

			processFailure(e);
			return false;

		}

	}

	protected void processFailure(final Throwable e) {

		reporter.requestFailedCount.inc();

		setAvailable(false);

		final String message = "" + e.getMessage();

		// if (amazonThrottleExceptions()) {
		// if (exceptionCache.getIfPresent(message) != null) {
		// return;
		// } else {
		// exceptionCache.put(message, "");
		// }
		// }

		log.error("amazon falilure", e);

	}

	@Override
	public boolean save(final String path, final File file) {

		reporter.requestSaveCount.inc();
		reporter.requestTotalCount.inc();

		try {

			final String bucket = config.bucket();

			final PutObjectRequest request = //
			new PutObjectRequest(bucket, PathHelp.rootLessPath(path), file);

			final PutObjectResult result = client.putObject(request);

			setAvailable(true);

			return true;

		} catch (final Exception e) {

			processFailure(e);
			return false;

		}

	}

	protected boolean setAvailable(final boolean next) {

		final boolean past = isAvailable;

		isAvailable = next;

		reporter.providerAvailable.value(isAvailable);

		return past;

	}

	@Override
	public synchronized void ensure() {
		stop();
		start();
	}

	@Override
	public synchronized void start() {

		if (config == null) {
			throw new IllegalStateException("config is missing");
		}

		if (client == null) {
			client = new AmazonS3Client(credentials());
			client.setEndpoint(config.endpoint());
		} else {
			// throw new IllegalStateException("client is present");
		}

		if (healthFuture == null) {
			final int period = config.healthPeriod();
			healthFuture = scheduler.scheduleAtFixedRate( //
					healtTask, 0, period, TimeUnit.SECONDS);
		} else {
			// throw new IllegalStateException("future is present");
		}

		checkCount = 0;

		log.info("\n\t ### start");

	}

	@Override
	public synchronized void stop() {

		if (healthFuture == null) {
			// throw new IllegalStateException("future is missing");
		} else {
			healthFuture.cancel(true);
			healthFuture = null;
		}

		if (client == null) {
			// throw new IllegalStateException("client is missing");
		} else {
			client.shutdown();
			client = null;
		}

		log.info("\n\t ### stop");

	}

	@Override
	public Reporter reporter() {
		return reporter;
	}

}
