/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.case_02;

import static org.junit.Assert.*;
import static org.sonatype.nexus.client.rest.BaseUrl.*;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource.*;
import static org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy.*;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.*;
import static org.sonatype.sisu.goodies.common.Varargs.*;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.M2GavCalculator;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.BeanScanning;
import org.sonatype.nexus.bundle.launcher.NexusBundle;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.rest.AuthenticationInfo;
import org.sonatype.nexus.client.rest.BaseUrl;
import org.sonatype.nexus.client.rest.NexusClientFactory;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.sonatype.sisu.maven.bridge.MavenModelResolver;
import org.sonatype.sisu.maven.bridge.support.ModelBuildingRequestBuilder;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonProvider;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;

@NexusStartAndStopStrategy(EACH_TEST)
public abstract class Base extends NexusRunningParametrizedITSupport {

	protected static interface Ready {
		boolean isReady();
	}

	protected final static M2GavCalculator gavCalc = new M2GavCalculator();

	protected static final Logger log = LoggerFactory.getLogger(Base.class);

	private static final String repoId = "releases";

	@Parameters
	public static Collection<Object[]> parameters() {

		return firstAvailableTestParameters(

		systemTestParameters(),

		testParameters($( //
				"org.sonatype.nexus:nexus-oss-webapp:zip:bundle" //
				)))

		.load();

	}

	@Inject
	private AmazonProvider amazonProvider;

	private boolean amazonProviderIsNew = true;

	@Inject
	@Named("remote-model-resolver-using-settings")
	private MavenModelResolver modelResolver;

	@Inject
	private NexusClientFactory nexusClientFactory;

	private NexusRestClient nexusRestletClient;

	private NexusClient nexusSystemClient;

	public Base(final String nexusBundleCoordinates) {
		super(nexusBundleCoordinates);
	}

	/** amazon service with bucket access */
	protected AmazonService amazonService() {

		if (amazonProviderIsNew) {

			final File file = configFile();

			final Map<String, String> props = ConfigBean.propsFrom(file);

			final ConfigBean config = new ConfigBean(props);

			amazonProvider.config(config);

			amazonProvider.start();

			amazonProviderIsNew = false;

			final Ready ready = new Ready() {
				@Override
				public boolean isReady() {
					return amazonProvider.isAvailable();
				}
			};

			sleep(1 * 1000, ready);

		}

		return amazonProvider;
	}

	protected void apply(final CapabilityResource item,
			final Map<String, String> props) {

		final Set<Entry<String, String>> entrySet = props.entrySet();

		for (final Entry<String, String> entry : entrySet) {
			apply(item, entry.getKey(), entry.getValue());
		}

	}

	protected void apply(final CapabilityResource item, final String key,
			final String value) {

		item.withProperty(//
		capabilityProperty().withKey(key).withValue(value));

	}

	protected void applyConfig(final boolean isEnabled,
			final Map<String, String> props) throws Exception {

		final CapabilityResource item = resourceItem();

		item.setEnabled(isEnabled);

		item.setNotes("timestamp=" + System.currentTimeMillis());

		apply(item, props);

		capabilities().update(item);

		inspector().waitForCalmPeriod(500);

	}

	/** configuration with amazon access */
	protected void applyConfigCustom() throws Exception {

		applyConfig(true, ConfigBean.propsFrom(configFile()));

	}

	/** configuration with NO amazon access */
	protected void applyConfigDefault() throws Exception {

		applyConfig(false, ConfigBean.defaultProps());

	}

	/** this project plug-in bundle */
	protected File bundleFile() {

		final File projectPom = new File("pom.xml");

		final ModelBuildingRequest request = ModelBuildingRequestBuilder
				.model().pom(projectPom);

		final Model model;
		try {
			model = modelResolver.resolveModel(request);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		final String artifactId = model.getArtifactId();
		final String version = model.getVersion();

		final String bundleName = artifactId + "-" + version + "-bundle.zip";

		final File bundleFile = new File("target", bundleName);

		return bundleFile;

	}

	/** capabilities rest client */
	protected Capabilities capabilities() {

		return nexusSystemClient.getSubsystem(Capabilities.class);

	}

	/** config file with amazon access */
	protected File configFile() {

		final String home = System.getProperty("user.home");

		final File folder = new File(home, ".amazon/carrotgarden");

		final File file = new File(folder, ConfigBean.NAME + ".conf");

		assertTrue(file.exists());

		return file;

	}

	@Override
	protected NexusBundleConfiguration configureNexus(
			final NexusBundleConfiguration configuration) {

		/** dependency plug-ins */
		configuration.addPlugins(

		artifactResolver().resolvePluginFromDependencyManagement(
				"org.sonatype.nexus.plugins", "nexus-capabilities-plugin")

		);

		/** project self plug-in */
		configuration.addPlugins(bundleFile());

		return configuration;

	}

	@Before
	public void configureClients() {

		/** nexus default credentials */
		final String username = "admin";
		final String password = "admin123";

		final NexusBundle nexus = nexus();

		final URL url = nexus.getUrl();

		//

		final BaseUrl baseUrl = baseUrlFrom(url);

		final AuthenticationInfo authInfo = new UsernamePasswordAuthenticationInfo(
				username, password);

		nexusSystemClient = nexusClientFactory.createFor(baseUrl, authInfo);

		//

		final TestContext context = new TestContext();
		context.setSecureTest(true);
		context.setNexusUrl(url.toString());
		context.setUsername(username);
		context.setPassword(password);

		nexusRestletClient = new NexusRestClient(context);

	}

	protected void deploy(final String path) throws Exception {
		deploy(path, fileSource(path));
	}

	protected void deploy(final String path, final File file) throws Exception {
		assertNotNull(path);
		assertNotNull(file);
		assertTrue(file.exists());
		final Gav gav = gavCalc.pathToGav(path);
		deployer().deployUsingGavWithRest(repoId(), gav, file);
	}

	protected DeployUtils deployer() {
		return new DeployUtils(nexusRestletClient);
	}

	/** attribute file in repository */
	protected File fileAttrib(final String path) {
		final File folder = repoRoot();
		final File file = new File(folder, ".nexus/attributes/" + path);
		return file;
	}

	/** original file in resources */
	protected File fileSource(final String path) {
		assertNotNull(path);
		final File folder = new File("resources/case_02/files");
		final File file = new File(folder, path);
		return file;
	}

	/** stored file item in repository */
	protected File fileTarget(final String path) {
		assertNotNull(path);
		final File folder = repoRoot();
		final File file = new File(folder, path);
		return file;
	}

	/** transient temporary file */
	protected File fileTransient() throws Exception {
		final File file = File.createTempFile("temp", null);
		file.deleteOnExit();
		return file;
	}

	protected EventInspectorsUtil inspector() {
		return new EventInspectorsUtil(nexusRestletClient);
	}

	protected boolean isOrdered(final long... source) {
		final long[] target = source.clone();
		Arrays.sort(target);
		return Arrays.equals(source, target);
	}

	protected boolean isSameFile(final File source, final File target)
			throws Exception {
		return FileTestingUtils.compareFileSHA1s(source, target);
	}

	protected String repoId() {
		return repoId;
	}

	protected File repoRoot() {
		final File work = nexus().getWorkDirectory();
		final File folder = new File(work, "storage/" + repoId());
		assertTrue(folder.exists());
		return folder;
	}

	/** locate default carrot resource */
	protected CapabilityListItemResource resourceEntry() {

		final String typeId = ConfigBean.NAME;

		final List<CapabilityListItemResource> itemList = capabilities().list();

		int count = 0;
		CapabilityListItemResource result = null;

		for (final CapabilityListItemResource item : itemList) {
			if (typeId.equals(item.getTypeId())) {
				count++;
				result = item;
			}
		}

		switch (count) {
		case 0:
			fail("missing typeId=" + typeId);
			break;
		case 1:
			assertNotNull(result);
			break;
		default:
			fail("multiple typeId=" + typeId);
			break;
		}

		return result;

	}

	/** locate default carrot resource */
	protected CapabilityResource resourceItem() {
		return capabilities().get(resourceEntry().getId());
	}

	/** work around guice bug; see super.scanning() */
	@Override
	public BeanScanning scanning() {
		return BeanScanning.ON;
	}

	protected void sleep(final long millis) {
		final Ready never = new Ready() {
			@Override
			public boolean isReady() {
				return false;
			}

		};
		sleep(millis, 1, never);
	}

	protected void sleep(final long millis, final int steps, final Ready state) {
		for (int k = 0; k < steps; k++) {
			if (state.isReady()) {
				return;
			}
			try {
				Thread.sleep(millis / steps);
			} catch (final Exception e) {
				return;
			}
		}
	}

	protected void sleep(final long millis, final Ready state) {
		sleep(millis, 10, state);
	}

}
