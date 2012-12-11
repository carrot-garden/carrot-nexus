package com.carrotgarden.nexus.aws.s3.publish.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder;

public class CapaHelp {

	public static boolean hasNoReference(final CapabilityRegistry registry,
			final CapabilityType type) {

		final List<CapabilityReference> referenceList = referenceList(registry,
				type);

		return referenceList.isEmpty();

	}

	public static List<CapabilityReference> referenceList(
			final CapabilityRegistry registry, final CapabilityType type) {

		final CapabilityReferenceFilterBuilder.CapabilityReferenceFilter filter = //
		CapabilityReferenceFilterBuilder.capabilities().withType(type);

		@SuppressWarnings("unchecked")
		final Collection<CapabilityReference> capabilities = //
		(Collection<CapabilityReference>) registry.get(filter);

		if (capabilities == null || capabilities.isEmpty()) {
			return Collections.emptyList();
		} else {
			return new ArrayList<CapabilityReference>(capabilities);
		}

	}

}
