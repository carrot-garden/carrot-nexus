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

import org.apache.maven.wagon.TransferFailedException;
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

import temp.zAmazonConfig;
import temp.zAmazonService;

import com.carrotgarden.nexus.aws.s3.publish.attribute.CarrotAttribute;

/**
 * @
 */
public class Main01 extends AbstractNexusIntegrationTest {

	static {

		System.setProperty("it.nexus.log.level", "INFO");

		staticLog.info("#### load");

		/**
		 * do not provide SecurityModule - guice injection bug
		 * <p>
		 * see {@link #startITPlexusContainer()}
		 */
		TestContainer.getInstance().startPlexusContainer(Main01.class);

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

	private zAmazonConfig amazonConfig;

	private zAmazonService amazonService;

	//

	@Mock
	private ApplicationConfiguration config;

	{
		log.info("#### init");
	}

	/** root config folder */
	private File configBase() {
		return new File(WORK_CONF_DIR);
	}

	@BeforeTest
	public void mockInit() throws Exception {

		MockitoAnnotations.initMocks(this);

		when(config.getConfigurationDirectory()).thenReturn(configBase());

		amazonConfig = new AmazonConfigMock(config);

		amazonService = new AmazonServiceMock(amazonConfig);

	}

	//

	/** configuration which has no amazon access */
	private void provideInvalidConfig() throws Exception {

		final File folder = new File("./src/main/resources");

		final File source = new File(folder, zAmazonConfig.PROPS_FILE);
		final File target = new File(configBase(), zAmazonConfig.PROPS_FILE);

		FileUtils.copyFile(source, target);

	}

	/** configuration with proper amazon access */
	private void provideValidConfig() throws Exception {

		final String home = System.getProperty("user.home");

		final File folder = new File(home, ".amazon/carrotgarden");

		final File source = new File(folder, zAmazonConfig.PROPS_FILE);
		final File target = new File(configBase(), zAmazonConfig.PROPS_FILE);

		FileUtils.copyFile(source, target);

	}

	/** attribute file in the test repo */
	private File repoAttrFile(final String path) {
		final String absPath = repoRoot().getAbsolutePath()
				+ "/.nexus/attributes/" + path;
		return new File(absPath);
	}

	/** dedicated test repo id */
	private String repoId() {
		return REPO_TEST_HARNESS_REPO;
	}

	/** artifact item file in the test repo */
	private File repoItemFile(final String path) {
		final String absPath = repoRoot().getAbsolutePath() + "/" + path;
		return new File(absPath);
	}

	/** root dir of test repo */
	private File repoRoot() {
		return new File(nexusWorkDir, "storage/" + repoId());
	}

	/** root url of test repo */
	private String repoUrl() {
		return getRepositoryUrl(repoId());
	}

	@Override
	public void startITPlexusContainer() {
		/**
		 * disable; see above static { }
		 */
	}

	/** normal deploy */
	@Test
	public void testDeployer() throws Exception {

		testDeployer("junit/junit/3.8.1/junit-3.8.1.pom");

		testDeployer("junit/junit/3.8.1/junit-3.8.1.jar");

		testDeployer("junit/junit/3.8.1/junit-3.8.1.zip");

	}

	/** test external deploy */
	private void testDeployer(final String path) throws Exception {

		log.info("### hello testDeployer");

		provideValidConfig();

		amazonService.checkAvailable();

		Assert.assertTrue(amazonService.isAvailable(),
				"amazon should be available");

		Assert.assertTrue(amazonService.kill(path), "amazon delete");

		final long timeStart = System.currentTimeMillis();

		getEventInspectorsUtil().waitForCalmPeriod();

		final File source = getTestFile(path);

		log.info("### source=" + source);

		Assert.assertTrue(source.exists(), "source present");

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

		Assert.assertEquals(map.get(CarrotAttribute.ATTR_IS_SAVED), "true",
				"attrigute is present");

		final String timeText = map.get(CarrotAttribute.ATTR_SAVE_TIME);

		final long timeSaved = Long.parseLong(timeText);

		Assert.assertTrue(timeStart < timeSaved && timeSaved < timeFinish,
				"attrigute time is in range");

		final File amazon = File.createTempFile("amazon-", "-test");

		log.info("### amazon=" + amazon);

		Assert.assertTrue(amazonService.load(path, amazon), "amazon retrive");

		Assert.assertTrue(FileTestingUtils.compareFileSHA1s(source, amazon),
				"source and amazon match");

	}

	/** should fail on client side when amazon is not available */
	@Test(expectedExceptions = { TransferFailedException.class })
	public void testFailure() throws Exception {

		log.info("### hello testFailure");

		provideInvalidConfig();

		amazonService.checkAvailable();

		Assert.assertFalse(amazonService.isAvailable(),
				"amazon should not be available");

		final String path = "invalid/invalid/1.1.2/invalid-1.1.2.jar";

		final File source = getTestFile(path);

		log.info("### source=" + source);

		Assert.assertTrue(source.exists(), "source present");

		log.info("### attempt to deploy");

		/** should blow up with exception */
		getDeployUtils().deployWithWagon("http", repoUrl(), source, path);

	}

	/** test internal scan */
	@Test
	public void testScanner() throws Exception {

		log.info("### hello testScanner");

		provideValidConfig();

		amazonService.checkAvailable();

		Assert.assertTrue(amazonService.isAvailable(),
				"amazon should be available");

		final String path = "scanner/scanner/1.0/scanner-1.0.jar";

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

		Assert.assertTrue(attrib.exists(), "attribute should be file present");

		final Map<String, String> map = fromJson(attrib);

		Assert.assertEquals(map.get(CarrotAttribute.ATTR_IS_SAVED), "true",
				"attribute entry is present");

		final String timeText = map.get(CarrotAttribute.ATTR_SAVE_TIME);

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
