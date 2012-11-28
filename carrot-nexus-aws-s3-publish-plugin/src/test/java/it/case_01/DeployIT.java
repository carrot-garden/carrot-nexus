/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.case_01;

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.plexus.util.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonConfig;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotFile;

public class DeployIT extends AbstractNexusIntegrationTest {

	static {

		System.setProperty("it.nexus.log.level", "INFO");

		staticLog.info("#### load");

		/** do not provide SecurityModule - guice injection bug */
		TestContainer.getInstance().startPlexusContainer(DeployIT.class);

	}
	{
		log.info("#### init");
	}

	private static Map<String, String> fromJson(final File file)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		@SuppressWarnings("unchecked")
		final Map<String, String> map = mapper.readValue(file, Map.class);

		return map;

	}

	private static void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startITPlexusContainer() {
		// disable; see above static { }
	}

	//

	@Mock
	private ApplicationConfiguration config;

	private AmazonConfig amazonConfig;

	private AmazonService amazonService;

	@BeforeTest
	public void mockInit() throws Exception {

		MockitoAnnotations.initMocks(this);

		when(config.getConfigurationDirectory()).thenReturn(configBase());

		amazonConfig = new AmazonConfigMock(config);

		amazonService = new AmazonServiceMock(amazonConfig);

	}

	//

	/** dedicated test repo id */
	private String repoId() {
		return REPO_TEST_HARNESS_REPO;
	}

	private String repoUrl() {
		return getRepositoryUrl(repoId());
	}

	/** root dir of test repo */
	private File repoRoot() {
		return new File(nexusWorkDir, "storage/" + repoId());
	}

	/** artifact item file in the test repo */
	private File repoItemFile(final String path) {
		final String absPath = repoRoot().getAbsolutePath() + "/" + path;
		return new File(absPath);
	}

	/** attribute file in the test repo */
	private File repoAttrFile(final String path) {
		final String absPath = repoRoot().getAbsolutePath()
				+ "/.nexus/attributes/" + path;
		return new File(absPath);
	}

	/** root config folder */
	private File configBase() {
		return new File(WORK_CONF_DIR);
	}

	@Test
	public void testDeployer() throws Exception {

		provideConfig();

		testDeployer("junit/junit/3.8.1/junit-3.8.1.pom");

		testDeployer("junit/junit/3.8.1/junit-3.8.1.jar");

		testDeployer("junit/junit/3.8.1/junit-3.8.1.zip");

	}

	/** test external deploy */
	private void testDeployer(final String path) throws Exception {

		log.info("### hello");

		Assert.assertTrue(amazonService.kill(path), "amazon delete");

		final long timeStart = System.currentTimeMillis();

		getEventInspectorsUtil().waitForCalmPeriod();

		final File source = getTestFile(path);

		log.info("### source=" + source);

		getDeployUtils().deployWithWagon("http", repoUrl(), source, path);

		getEventInspectorsUtil().waitForCalmPeriod();

		final long timeFinish = System.currentTimeMillis();

		final File target = repoItemFile(path);

		log.info("### target=" + target);

		Assert.assertTrue(FileTestingUtils.compareFileSHA1s(source, target),
				"source and target match");

		final File attrib = repoAttrFile(path);

		log.info("### attrib=" + attrib);

		final Map<String, String> map = fromJson(attrib);

		Assert.assertEquals(map.get(CarrotFile.ATTR_IS_SAVED), "true",
				"attrigute is present");

		final String timeText = map.get(CarrotFile.ATTR_SAVE_TIME);

		final long timeSaved = Long.parseLong(timeText);

		Assert.assertTrue(timeStart < timeSaved && timeSaved < timeFinish,
				"attrigute time is in range");

		final File amazon = File.createTempFile("amazon-", "-test");

		log.info("### amazon=" + amazon);

		Assert.assertTrue(amazonService.load(path, amazon), "amazon retrive");

		Assert.assertTrue(FileTestingUtils.compareFileSHA1s(source, amazon),
				"source and amazon match");

	}

	private void provideConfig() throws Exception {

		final String home = System.getProperty("user.home");

		final File amazon = new File(home, ".amazon/carrotgarden");

		final File source = new File(amazon, AmazonConfig.PROPS_FILE);
		final File target = new File(configBase(), AmazonConfig.PROPS_FILE);

		FileUtils.copyFile(source, target);

	}

	/** test internal scan */
	@Test
	public void testScanner() throws Exception {

		log.info("### hello");

		provideConfig();

		final String path = "test/test/1.0/artifact-1.0.jar";

		Assert.assertTrue(amazonService.kill(path), "amazon delete");

		final File source = getTestFile(path);

		log.info("### source=" + source);

		Assert.assertTrue(source.exists(), "source present");

		final File target = repoItemFile(path);

		log.info("### target=" + target);

		Assert.assertFalse(target.exists(), "target missing");

		final File attrib = repoAttrFile(path);

		log.info("### attrib=" + attrib);

		final long timeStart = System.currentTimeMillis();

		Assert.assertFalse(attrib.exists(), "attribute missing");

		FileUtils.copyFile(source, target);

		log.info("### wait for scan");

		sleep(10 * 1000);

		final long timeFinish = System.currentTimeMillis();

		log.info("### scan is ready");

		getEventInspectorsUtil().waitForCalmPeriod();

		Assert.assertTrue(attrib.exists(), "attribute file present");

		final Map<String, String> map = fromJson(attrib);

		Assert.assertEquals(map.get(CarrotFile.ATTR_IS_SAVED), "true",
				"attribute entry is present");

		final String timeText = map.get(CarrotFile.ATTR_SAVE_TIME);

		final long timeSaved = Long.parseLong(timeText);

		Assert.assertTrue(timeStart < timeSaved && timeSaved < timeFinish,
				"attribute time is in range");

		final File amazon = File.createTempFile("amazon-", "-test");

		log.info("### amazon=" + amazon);

		Assert.assertTrue(amazonService.load(path, amazon), "amazon retrive");

		Assert.assertTrue(FileTestingUtils.compareFileSHA1s(source, amazon),
				"source and amazon match");

	}

}
