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
import org.sonatype.nexus.proxy.repository.Repository;

import com.carrotgarden.nexus.aws.s3.publish.scanner.ScannerService;
import com.carrotgarden.nexus.aws.s3.publish.util.Util;

@Named
@Singleton
public class CarrotRepositoryCustomizer implements RepositoryCustomizer {

	public static final String NAME = "CarrotRepositoryCustomizer";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{

		log.info("init " + NAME);

	}

	@Inject
	private ScannerService scannerService;

	@Override
	public boolean isHandledRepository(final Repository repository) {

		return Util.isProperRepository(repository);

	}

	@Override
	public void configureRepository(final Repository repository)
			throws ConfigurationException {

		scannerService.register(repository);

	}

}
