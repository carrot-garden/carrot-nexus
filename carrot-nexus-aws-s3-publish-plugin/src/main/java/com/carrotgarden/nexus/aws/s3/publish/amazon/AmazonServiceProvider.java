/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.amazon;

import static com.carrotgarden.nexus.aws.s3.publish.util.PathHelp.*;

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
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;
import com.carrotgarden.nexus.aws.s3.publish.mailer.CarrotMailer;
import com.carrotgarden.nexus.aws.s3.publish.mailer.Report;
import com.carrotgarden.nexus.aws.s3.publish.metrics.AmazonReporter;
import com.carrotgarden.nexus.aws.s3.publish.metrics.Reporter;
import com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp;
import com.google.inject.assistedinject.Assisted;
import com.yammer.metrics.core.Gauge;

/**
 */
@Named(AmazonServiceProvider.NAME)
public class AmazonServiceProvider implements AmazonService, AmazonManager {

	public static final String NAME = "carrot.amazon.provider";

	private static final NexusThreadFactory threadFactory = //
	new NexusThreadFactory("carrot", NAME);

	private static final ScheduledExecutorService scheduler = //
	Executors.newScheduledThreadPool(1, threadFactory);

	private final AmazonReporter reporter;
	private final CarrotMailer mailer;
	private final ConfigEntry entry;

	@Inject
	public AmazonServiceProvider( //
			final CarrotMailer mailer, //
			final AmazonReporter reporter, //
			@Assisted final ConfigEntry entry //

	) {

		this.entry = entry;
		this.mailer = mailer;
		this.reporter = reporter;

		reporter.newGauge("provider failure", new Gauge<String>() {
			@Override
			public String value() {
				return "" + failure;
			}
		});
		reporter.newGauge("provider available", new Gauge<Boolean>() {
			@Override
			public Boolean value() {
				return isAvailable();
			}
		});

	}

	private long checkCount;

	private volatile AmazonS3Client client;

	private volatile ConfigBean configBean;

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

			final GetBucketLocationRequest request = //
			new GetBucketLocationRequest(mavenBucket());

			final String result = client.getBucketLocation(request);

			setAvailable(true, null);

		} catch (final Exception e) {

			setAvailable(false, e);

		}

		mailer.sendAmazonReport(Report.AMAZON_HEALTH_REPORT, entry, this);

		checkCount++;

	}

	@Override
	public synchronized void configure(final ConfigBean config) {
		this.configBean = config;
	}

	private AWSCredentials credentials() {

		final String username = configBean.awsAccess();
		final String password = configBean.awsSecret();

		return new BasicAWSCredentials(username, password);

	}

	@Override
	public boolean isAvailable() {
		return isAvailable;
	}

	private boolean isFirstScheck() {
		return checkCount == 0;
	}

	private String mavenBucket() {
		return configBean.bucket();
	}

	private String mavenRepoKey(final String path) {
		return configBean.prefix() + rootFullPath(path);
	}

	@Override
	public boolean kill(final String path) {

		reporter.requestKillCount.inc();
		reporter.requestTotalCount.inc();

		try {

			final DeleteObjectRequest request = //
			new DeleteObjectRequest(mavenBucket(), mavenRepoKey(path));

			client.deleteObject(request);

			setAvailable(true, null);

			return true;

		} catch (final Exception e) {

			setAvailable(true, e);

			return false;

		}

	}

	@Override
	public boolean load(final String path, final File file) {

		reporter.requestLoadCount.inc();
		reporter.requestTotalCount.inc();

		try {

			final GetObjectRequest request = //
			new GetObjectRequest(mavenBucket(), mavenRepoKey(path));

			final ObjectMetadata result = client.getObject(request, file);

			reporter.fileLoadCount.inc();
			reporter.fileLoadSize.inc(file.length());
			reporter.fileLoadWatch.add(file);

			setAvailable(true, null);

			return true;

		} catch (final AmazonS3Exception e) {

			switch (e.getStatusCode()) {
			case 404:
				log.error("path={} code={}", path, e.getErrorCode());
				break;
			default:
				setAvailable(true, e);
				break;
			}
			return false;

		} catch (final Exception e) {

			setAvailable(false, e);

			return false;

		}

	}

	protected void processFailure(final Throwable e) {

		failure = e;

		reporter.requestFailedCount.inc();

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

			final PutObjectRequest request = //
			new PutObjectRequest(mavenBucket(), mavenRepoKey(path), file);

			final PutObjectResult result = client.putObject(request);

			reporter.fileSaveCount.inc();
			reporter.fileSaveSize.inc(file.length());
			reporter.fileSaveWatch.add(file);

			setAvailable(true, null);

			return true;

		} catch (final Exception e) {

			setAvailable(false, e);

			return false;

		}

	}

	private String amazonId() {
		return "[" + entry.configId() + "]";
	}

	protected void setAvailable(final boolean nextAvailable, final Throwable e) {

		failure = e;

		final boolean pastAvailable = isAvailable;

		final boolean isChanged = pastAvailable ^ nextAvailable;

		isAvailable = nextAvailable;

		if (isChanged && isAvailable) {

			log.warn("amazon became available : {}", amazonId());

			mailer.sendAmazonReport(Report.AMAZON_AVAILABLE, entry, this);

		}

		if (isChanged && !isAvailable) {

			log.error("amazon became unavailable : {}", amazonId());

			mailer.sendAmazonReport(Report.AMAZON_UNAVAILABLE, entry, this);

		}

	}

	@Override
	public synchronized void ensure() {

		if (isRunning) {
			stop();
			start();
		} else {
			start();
		}

	}

	private boolean isRunning;

	@Override
	public synchronized void start() {

		if (configBean == null) {
			throw new IllegalStateException("config is missing");
		}

		if (client == null) {
			client = new AmazonS3Client(credentials());
			client.setEndpoint(configBean.endpoint());
		} else {
			// throw new IllegalStateException("client is present");
		}

		if (healthFuture == null) {
			final int period = configBean.healthPeriod();
			healthFuture = scheduler.scheduleAtFixedRate( //
					healtTask, 0, period, TimeUnit.SECONDS);
		} else {
			// throw new IllegalStateException("future is present");
		}

		checkCount = 0;

		isRunning = true;

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

		isRunning = false;

		log.info("\n\t ### stop");

	}

	@Override
	public Reporter reporter() {
		return reporter;
	}

	private Throwable failure;

	@Override
	public Throwable failure() {
		return failure;
	}

}
