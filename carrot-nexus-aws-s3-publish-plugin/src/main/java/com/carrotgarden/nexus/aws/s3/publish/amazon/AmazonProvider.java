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
import org.sonatype.nexus.plugins.capabilities.Condition;
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
import com.carrotgarden.nexus.aws.s3.publish.condition.ConditionFactory;
import com.carrotgarden.nexus.aws.s3.publish.condition.ManagedCondition;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;
import com.carrotgarden.nexus.aws.s3.publish.util.Util;

@Named
public class AmazonProvider implements AmazonService, AmazonManager {

	private static final NexusThreadFactory threadFactory = //
	new NexusThreadFactory("carrot", "amazon-provider");

	private static final ScheduledExecutorService scheduler = //
	Executors.newScheduledThreadPool(1, threadFactory);

	private volatile AmazonS3Client client;

	private final ManagedCondition condition;

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

	@Inject
	public AmazonProvider(final ConditionFactory conditionFactory) {
		this.condition = conditionFactory.managed("amazon-provider");
	}

	@Override
	public Condition condition() {
		return condition;
	}

	private long checkCount;

	private boolean isFirstScheck() {
		return checkCount == 0;
	}

	private synchronized void checkAvailable() {

		try {

			final String bucket = config.bucket();

			final GetBucketLocationRequest request = //
			new GetBucketLocationRequest(bucket);

			final String result = client.getBucketLocation(request);

			if (!setAvailable(true) || isFirstScheck()) {
				log.info("\n\t ### amazon provider available");
			}

		} catch (final Exception e) {

			if (setAvailable(false) || isFirstScheck()) {
				log.error("\n\t ### amazon provider unavailable", e);
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

	@Override
	public boolean kill(final String path) {

		if (!isAvailable()) {
			return false;
		}

		try {

			final String bucket = config.bucket();

			final DeleteObjectRequest request = //
			new DeleteObjectRequest(bucket, Util.rootLessPath(path));

			client.deleteObject(request);

			return true;

		} catch (final Exception e) {

			processFailure(e);
			return false;

		}

	}

	@Override
	public boolean load(final String path, final File file) {

		if (!isAvailable()) {
			return false;
		}

		try {

			final String bucket = config.bucket();

			final GetObjectRequest request = //
			new GetObjectRequest(bucket, Util.rootLessPath(path));

			final ObjectMetadata result = client.getObject(request, file);

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

	@Override
	public boolean save(final String path, final File file) {

		if (!isAvailable()) {
			return false;
		}

		try {

			final String bucket = config.bucket();

			final PutObjectRequest request = //
			new PutObjectRequest(bucket, Util.rootLessPath(path), file);

			final PutObjectResult result = client.putObject(request);

			return true;

		} catch (final Exception e) {

			processFailure(e);
			return false;

		}

	}

	protected void processFailure(final Throwable e) {

		setAvailable(false);

		log.error("provider failure", e);

	}

	protected boolean setAvailable(final boolean next) {

		final boolean past = isAvailable;

		isAvailable = next;

		condition.setSatisfied(isAvailable);

		return past;

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
			throw new IllegalStateException("client is present");
		}

		if (healthFuture == null) {
			final int period = config.healthPeriod();
			healthFuture = scheduler.scheduleAtFixedRate( //
					healtTask, 0, period, TimeUnit.SECONDS);
		} else {
			throw new IllegalStateException("future is present");
		}

		checkCount = 0;

		log.info("\n\t ### start");

	}

	@Override
	public synchronized void stop() {

		if (healthFuture == null) {
			throw new IllegalStateException("future is missing");
		} else {
			healthFuture.cancel(true);
			healthFuture = null;
		}

		if (client == null) {
			throw new IllegalStateException("client is missing");
		} else {
			client.shutdown();
			client = null;
		}

		log.info("\n\t ### stop");

	}

}
