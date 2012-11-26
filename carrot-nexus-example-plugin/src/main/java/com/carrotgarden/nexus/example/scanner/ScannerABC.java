package com.carrotgarden.nexus.example.scanner;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.item.StorageFileItem;

@Named(ScannerABC.NAME)
public class ScannerABC implements Scanner {

	public static final String NAME = "ABC";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public boolean hasVirus(final StorageFileItem file) {

		// DO THE JOB HERE
		System.out
				.println("Kung fu VirusScanner --- scanning for viruses on item: "
						+ file.getPath());

		// simulating virus hit by having the filename contain the "infected"
		// string

		return file.getName().contains("infected");

	}

}
