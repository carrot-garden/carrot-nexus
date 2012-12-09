/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.case_02;

import static org.junit.Assert.*;
import static org.sonatype.nexus.test.utils.EmailUtil.*;
import it.any.TestAny;
import it.util.TestHelp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.rest.model.SmtpSettingsResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

import com.carrotgarden.nexus.aws.s3.publish.config.Form;
import com.carrotgarden.nexus.aws.s3.publish.mailer.Report;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.SimpleStoredMessage;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class TestMailer extends TestAny {

	protected static final Logger log = LoggerFactory
			.getLogger(TestMailer.class);

	private static final int port = 2525;

	private static GreenMail server;
	private static GreenMailUser systemUser;
	private static GreenMailUser testerUser;
	private static final String testerEmail = "test_user@sonatype.org";

	@AfterClass
	public static void done() {

		log.info("server done");

		if (server != null) {
			server.stop();
			server = null;
		}

	}

	@BeforeClass
	public static void init() {

		final ServerSetup setup = new ServerSetup(port, null,
				ServerSetup.PROTOCOL_SMTP);

		server = new GreenMail(setup);

		/** smtp sender */
		systemUser = server.setUser(USER_EMAIL, USER_USERNAME, USER_PASSWORD);

		/** imap reader */
		testerUser = server.setUser(testerEmail, USER_USERNAME, USER_PASSWORD);

		log.info("server init");

		server.start();

	}

	public TestMailer(final String nexusBundleCoordinates) {
		super(nexusBundleCoordinates);
	}

	private List<MimeMessage> messageRead() throws Exception {

		server.waitForIncomingEmail(5 * 1000, 1);

		final MailFolder inbox = //
		server.getManagers().getImapHostManager().getInbox(testerUser);

		@SuppressWarnings("unchecked")
		final List<SimpleStoredMessage> storedList = inbox.getMessages();

		final List<MimeMessage> mimeList = new ArrayList<MimeMessage>();

		for (final SimpleStoredMessage stored : storedList) {
			final MimeMessage mime = stored.getMimeMessage();
			mimeList.add(mime);
		}

		inbox.deleteAllMessages();

		return mimeList;

	}

	// @Test
	public void testBasic() throws Exception {

		testSend();
		testSend();
		testSend();

	}

	@Test
	public void testMailer() throws Exception {

		testSend();
		messageRead();

		/**
		 */
		final Map<String, String> props = Form.propsFrom(TestHelp.configFile());
		props.put("combo-id", repoId());
		props.put("enable-email", "true");
		props.put("email-address", testerEmail);
		props.put("enable-scanner", "false");

		applyConfig(true, props);

		testMailer("mail/mail/1.0/mail-1.0.jar");

	}

	private void testMailer(final String path) throws Exception {

		assertTrue("amazon delete", amazonService().kill(path));

		final File source = fileSource(path);
		final File target = fileTarget(path);
		final File attrib = fileAttrib(path);

		assertTrue("source present", source.exists());
		assertFalse("target missing", target.exists());
		assertFalse("attrib missing", attrib.exists());

		deploy(path);

		inspector().waitForCalmPeriod(500);

		assertTrue("target present", target.exists());
		assertTrue("attrib present", attrib.exists());

		final List<MimeMessage> messageList = messageRead();

		for (final MimeMessage message : messageList) {
			log.info("### head : \n{}", GreenMailUtil.getHeaders(message));
		}

		assertEquals(4, messageList.size());

		assertTrue(GreenMailUtil.getHeaders(messageList.get(0)).contains(
				Report.PLUGIN_ENABLED.name()));
		assertTrue(GreenMailUtil.getHeaders(messageList.get(1)).contains(
				Report.AMAZON_AVAILABLE.name()));
		assertTrue(GreenMailUtil.getHeaders(messageList.get(2)).contains(
				Report.AMAZON_HEALTH_REPORT.name()));
		assertTrue(GreenMailUtil.getHeaders(messageList.get(3)).contains(
				Report.DEPLOY_SUCCESS.name()));

		assertTrue("amazon delete", amazonService().kill(path));

	}

	/** configure nexus smpt and send test email */
	public void testSend() throws Exception {

		final SmtpSettingsResource settings = new SmtpSettingsResource();
		settings.setHost("localhost");
		settings.setPort(port);
		settings.setUsername(USER_USERNAME);
		settings.setPassword(USER_PASSWORD);
		settings.setSystemEmailAddress(USER_EMAIL);
		settings.setTestEmail(testerEmail);

		final Status status = SettingsMessageUtil.save(settings);

		log.info("status : {}", status);

		final List<MimeMessage> messageList = messageRead();

		assertEquals(1, messageList.size());

		final MimeMessage message = messageList.get(0);

		final String head = GreenMailUtil.getHeaders(message);
		final String body = GreenMailUtil.getBody(message);

		log.info("head : {}", head);
		log.info("body : {}", body);

		assertTrue(head.contains("SMTP Configuration validation"));
		assertTrue(body.contains("Your current SMTP configuration is valid"));

	}

}
