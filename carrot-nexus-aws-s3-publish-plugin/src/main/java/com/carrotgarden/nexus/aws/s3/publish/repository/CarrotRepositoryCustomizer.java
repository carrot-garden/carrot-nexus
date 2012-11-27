package com.carrotgarden.nexus.aws.s3.publish.repository;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.plugins.RepositoryCustomizer;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;

import com.carrotgarden.nexus.aws.s3.publish.request.CarrotRequestProcessor;

@Named(CarrotRepositoryCustomizer.NAME)
public class CarrotRepositoryCustomizer implements RepositoryCustomizer {

	public static final String NAME = "CarrotRepositoryCustomizer";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Inject
	@Named(CarrotRequestProcessor.NAME)
	private RequestProcessor processor;

	@Inject
	private ApplicationConfiguration config;

	@Override
	public boolean isHandledRepository(final Repository repository) {

		return true;

	}

	@Override
	public void configureRepository(final Repository repository)
			throws ConfigurationException {

		try {

			final File root = repoRoot(repository);

			log.info("root : {} / {}", root, root.exists());

			repository.getRequestProcessors().put( //
					CarrotRequestProcessor.NAME, processor);

		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	private File repoRoot(final Repository repository) throws Exception {

		final String url = repository.getLocalUrl();

		final File root;

		if (url == null) {

			/** original */

			final String repoId = repository.getId();

			final File work = config.getWorkingDirectory();

			root = new File(new File(work, "storage"), repoId);

		} else {

			/** customized */

			root = new File(new URL(url).toURI());

		}

		log.info("root : {} / {}", root, root.exists());

		return root;

	}

	private void iterate(final File root, final MavenRepository repo) {

		final String path = "";

		final Gav gav = repo.getGavCalculator().pathToGav(path);

		if (gav == null || gav.isSignature() || gav.isHash()) {
			return;
		}

		if (gav.isSnapshot()) {
			return;
		}

		log.info("path={}", path);

	}

}
