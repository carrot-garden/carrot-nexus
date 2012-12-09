/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.mailer;

import static com.carrotgarden.nexus.aws.s3.publish.util.ConfigHelp.*;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.nexus.email.NexusEmailer;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonManager;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigCapability;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;
import com.carrotgarden.nexus.aws.s3.publish.task.ScannerTask;

/**
 * mail report generator
 */
@Singleton
@Named(CarrotMailer.NAME)
public class CarrotMailer {

	public static final String NAME = "carrot.mailer";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final NexusEmailer nexusEmailer;

	@Inject
	public CarrotMailer(//
			@Nullable final NexusEmailer nexusEmailer //
	) {

		this.nexusEmailer = nexusEmailer;

	}

	private static String subject(final Report report, final ConfigEntry entry) {

		return report + " [" + entry.configId() + "] " + pluginName();

	}

	private static String subject(final Report report, final ScannerTask task,
			final ConfigEntry entry) {

		return report + "/" + task.configType() + " [" + entry.configId()
				+ "] " + pluginName();

	}

	public void send(final String email, final String subject,
			final String message) {

		if (nexusEmailer == null) {
			log.warn("nexus mailer not available");
			return;
		}

		final MailRequest request = //
		nexusEmailer.getDefaultMailRequest(subject, message);

		request.getToAddresses().add(new Address(email));

		/** asynchronous */
		nexusEmailer.sendMail(request);

	}

	public void send(final List<String> emailList, final String subject,
			final String message) {

		for (final String email : emailList) {
			send(email, subject, message);
		}

	}

	public void sendAmazonReport(final Report report, final ConfigEntry entry,
			final AmazonManager manager) {

		if (!entry.isSubscribed(report)) {
			return;
		}

		final String subject;
		final String message;

		switch (report) {
		case AMAZON_AVAILABLE:
			subject = subject(report, entry);
			message = subject;
			break;
		case AMAZON_UNAVAILABLE:
			subject = subject(report, entry);
			message = subject + "\n\n" + manager.failure();
			break;
		case AMAZON_HEALTH_REPORT:
			subject = subject(report, entry);
			message = subject + "\n\n" + manager.reporter().report();
			break;
		default:
			log.error("wrong report : {}", report);
			return;
		}

		send(entry.reportEmailList(), subject, message);

	}

	public void sendDeployReport(final Report report, final ConfigEntry entry,
			final Repository repo, final StorageFileItem item) {

		if (!entry.isSubscribed(report)) {
			return;
		}

		final String repoId = repo.getId();
		final String itemPath = item.getPath();
		final Throwable failure = entry.amazonService().failure();

		final String subject;
		final String message;

		switch (report) {
		case DEPLOY_SUCCESS:
			subject = subject(report, entry);
			message = subject + "\n" + repoId + " : " + itemPath;
			break;
		case DEPLOY_FAILURE:
			subject = subject(report, entry);
			message = subject + "\n" + repoId + " : " + itemPath + "\n"
					+ failure;
			break;
		default:
			log.error("wrong report : {}", report);
			return;
		}

		send(entry.reportEmailList(), subject, message);

	}

	public void sendScannerReport(final Report report, final ConfigEntry entry,
			final ScannerTask task) {

		if (!entry.isSubscribed(report)) {
			return;
		}

		final String subject;
		final String message;

		switch (report) {
		case SCANNER_TASK_SUCCESS:
		case SCANNER_TASK_FAILURE:
			subject = subject(report, task, entry);
			message = subject + "\n\n" + task.failure();
			break;
		case SCANNER_TASK_SUCCESS_REPORT:
		case SCANNER_TASK_FAILURE_REPORT:
			subject = subject(report, task, entry);
			message = subject + "\n\n" + task.reporter().report();
			break;
		default:
			log.error("wrong report : {}", report);
			return;
		}

		send(entry.reportEmailList(), subject, message);

	}

	public void sendPluginReport(final Report report,
			final ConfigCapability entry) {

		final String subject = subject(report, entry);
		final String message = subject;

		send(entry.reportEmailList(), subject, message);

	}

}
