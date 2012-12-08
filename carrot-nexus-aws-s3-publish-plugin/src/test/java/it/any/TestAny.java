/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it.any;

import static org.junit.Assert.*;
import static org.sonatype.nexus.client.rest.BaseUrl.*;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource.*;
import static org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy.*;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.*;
import static org.sonatype.sisu.goodies.common.Varargs.*;
import it.util.Ready;
import it.util.TestHelp;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.index.artifact.M2GavCalculator;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
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
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.WagonDeployer;
import org.sonatype.nexus.testsuite.support.NexusRunningITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.sonatype.sisu.maven.bridge.MavenModelResolver;
import org.sonatype.sisu.maven.bridge.support.ModelBuildingRequestBuilder;

import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonFactory;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonManager;
import com.carrotgarden.nexus.aws.s3.publish.amazon.AmazonService;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigDescriptor;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;
import com.carrotgarden.nexus.aws.s3.publish.config.Form;

@RunWith(Parameterized.class)
@NexusStartAndStopStrategy(EACH_TEST)
public abstract class TestAny extends NexusRunningITSupport {

	protected final static M2GavCalculator gavCalc = new M2GavCalculator();

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

	// @Inject
	// private NexusEmailer nexusEmailer;

	@Inject
	private AmazonFactory amazonFactory;

	@Inject
	@Named("remote-model-resolver-using-settings")
	private MavenModelResolver modelResolver;

	@Inject
	private NexusClientFactory nexusClientFactory;

	private NexusRestClient nexusRestletClient;

	private NexusClient nexusSystemClient;

	public TestAny(final String nexusBundleCoordinates) {
		super(nexusBundleCoordinates);
	}

	private AmazonManager amazonProvider;

	/** testing amazon service with bucket access */
	protected AmazonService amazonService() {

		if (amazonProvider == null) {

			final ConfigEntry configEntry = Mockito.mock(ConfigEntry.class);

			Mockito.when(configEntry.configId()).thenReturn("tester");

			amazonProvider = amazonFactory.create(configEntry);

			final File file = TestHelp.configFile();

			final Map<String, String> props = Form.propsFrom(file);

			final ConfigBean configBean = new ConfigBean(props);

			// log.info("### configBean : \n{}", configBean);

			amazonProvider.configure(configBean);

			amazonProvider.ensure();

			final Ready ready = new Ready() {
				@Override
				public boolean isReady() {
					return amazonProvider.isAvailable();
				}
			};

			TestHelp.sleep(3 * 1000, ready);

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

	/** this project plug-in bundle.zip; must be built */
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

		/** */

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

		final TestContext context = TestContainer.getInstance()
				.getTestContext();

		context.setSecureTest(true);
		context.setNexusUrl(url.toString());
		context.setUsername(username);
		context.setPassword(password);

		nexusRestletClient = new NexusRestClient(context);

	}

	/** deploy existing test resource file */
	protected void deploy(final String path) throws Exception {
		assertNotNull(path);
		deploy(path, fileSource(path));
	}

	protected String repoUrl() {
		return nexus().getUrl() + "content/repositories/" + repoId() + "/";
	}

	/** deploy via rest */
	protected void deploy(final String path, final File file) throws Exception {

		assertNotNull(path);
		assertNotNull(file);
		assertTrue(file.exists());

		// final Gav gav = gavCalc.pathToGav(path);
		// deployer().deployUsingGavWithRest(repoId(), gav, file);

		deployer().deployWithWagon("http", repoUrl(), file, path);

	}

	protected DeployUtils deployer() {

		final WagonDeployer.Factory wagonFactory = new WagonDeployer.Factory() {

			@Override
			public Wagon get(final String protocol) {
				return new LightweightHttpWagon();
			}

		};

		return new DeployUtils(nexusRestletClient, wagonFactory);

	}

	/** attribute file in repository */
	protected File fileAttrib(final String path) {
		final File folder = repoRoot();
		final File file = new File(folder, ".nexus/attributes/" + path);
		return file;
	}

	/** original file in src/test/it-resources */
	protected File fileSource(final String path) {
		assertNotNull(path);
		final File file = testData().resolveFile(path);
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

	/** rest event state lookup */
	protected EventInspectorsUtil inspector() {
		return new EventInspectorsUtil(nexusRestletClient);
	}

	/** test repo id */
	protected String repoId() {
		return repoId;
	}

	/** nexus repository root folder for test repo id */
	protected File repoRoot() {
		final File work = nexus().getWorkDirectory();
		final File folder = new File(work, "storage/" + repoId());
		assertTrue(folder.exists());
		return folder;
	}

	/** locate default plug-in configuration */
	protected CapabilityListItemResource resourceEntry() {

		final String typeId = ConfigDescriptor.NAME;

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

	/** locate default plug-in configuration */
	protected CapabilityResource resourceItem() {
		return capabilities().get(resourceEntry().getId());
	}

	/** work around plexus/guice bug; see super.scanning() */
	@Override
	public BeanScanning scanning() {
		return BeanScanning.ON;
	}

}
