/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.repository;

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

import com.carrotgarden.nexus.aws.s3.publish.scanner.CarrotScanner;
import com.carrotgarden.nexus.aws.s3.publish.storage.CarrotStorageProvider;

@Singleton
@Named(CarrotCustomizerProvider.NAME)
public class CarrotCustomizerProvider implements RepositoryCustomizer {

	public static final String NAME = "carrot.repo.customizer";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{

		log.info("init " + NAME);

	}

	@Inject
	private CarrotScanner carrotScanner;

	@Inject
	@Named(CarrotStorageProvider.NAME)
	private LocalRepositoryStorage carrotStorage;

	/** handle only local maven */
	@Override
	public boolean isHandledRepository(final Repository repository) {

		final LocalRepositoryStorage storage = repository.getLocalStorage();

		final boolean isLocal = storage instanceof DefaultFSLocalRepositoryStorage;

		final boolean isMaven = repository instanceof AbstractMavenRepository;

		return isLocal && isMaven;

	}

	@Override
	public void configureRepository(final Repository repository)
			throws ConfigurationException {

		// carrotScanner.register(repository);

		repository.setLocalStorage(carrotStorage);

		log.info("customized repo : {}", repository.getId());

	}

}
