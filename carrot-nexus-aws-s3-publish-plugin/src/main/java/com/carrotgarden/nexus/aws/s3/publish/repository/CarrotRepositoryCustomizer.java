/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.repository;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.plugins.RepositoryCustomizer;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

import com.carrotgarden.nexus.aws.s3.publish.storage.CarrotRepositoryStorage;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;

/**
 * replace local storage
 */
@Singleton
@Named(CarrotRepositoryCustomizer.NAME)
public class CarrotRepositoryCustomizer implements RepositoryCustomizer {

	public static final String NAME = "carrot.repo.customizer";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** handle only local maven */
	@Override
	public boolean isHandledRepository(final Repository repository) {

		final LocalRepositoryStorage storage = repository.getLocalStorage();

		final boolean isLocal = storage instanceof DefaultFSLocalRepositoryStorage;

		final boolean isMaven = repository instanceof AbstractMavenRepository;

		return isLocal && isMaven;

	}

	private final Counter metricsRepoCount = Metrics.newCounter(getClass(),
			"customized repositories count");

	private final Set<String> metricsRepoSet = new HashSet<String>();

	private final LocalRepositoryStorage carrotStorage;

	@Inject
	public CarrotRepositoryCustomizer( //
			@Named(CarrotRepositoryStorage.NAME) final LocalRepositoryStorage carrotStorage //
	) {

		this.carrotStorage = carrotStorage;

		Metrics.newGauge(getClass(), "customized repositories list",
				new Gauge<String>() {
					@Override
					public String value() {
						return metricsRepoSet.toString();
					}
				});

	}

	@Override
	public void configureRepository(final Repository repository)
			throws ConfigurationException {

		final String repoId = repository.getId();

		metricsRepoSet.add(repoId);

		metricsRepoCount.inc();

		repository.setLocalStorage(carrotStorage);

		log.info("customized repository : {} by {}", repoId, NAME);

	}

}
