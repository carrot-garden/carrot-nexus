package com.carrotgarden.nexus.aws.s3.publish.mail;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailRequestStatus;
import org.sonatype.nexus.email.NexusEmailer;

@Named
@Singleton
public class CarrotMailer {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private NexusEmailer nexusEmailer;

	public void send(final String email, final String subject,
			final String message) {

		final MailRequest request = //
		nexusEmailer.getDefaultMailRequest(subject, message);

		request.getToAddresses().add(new Address(email));

		final MailRequestStatus status = nexusEmailer.sendMail(request);

		if (status.isSent()) {
			return;
		}

		log.error("failed to send email", status.getErrorCause());

	}

}
