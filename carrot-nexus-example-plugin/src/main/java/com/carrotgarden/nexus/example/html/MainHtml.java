package com.carrotgarden.nexus.example.html;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

public class MainHtml implements NexusIndexHtmlCustomizer {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public String getPreHeadContribution(final Map<String, Object> context) {
		return "hello1";
	}

	@Override
	public String getPostHeadContribution(final Map<String, Object> context) {
		return "hello2";
	}

	@Override
	public String getPreBodyContribution(final Map<String, Object> context) {
		return "hello3";
	}

	@Override
	public String getPostBodyContribution(final Map<String, Object> context) {
		return "hello4";
	}

}
