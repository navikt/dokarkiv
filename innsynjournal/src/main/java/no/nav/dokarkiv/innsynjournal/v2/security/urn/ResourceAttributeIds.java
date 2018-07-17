package no.nav.dokarkiv.innsynjournal.v2.security.urn;

import no.nav.modig.security.tilgangskontroll.URN;

/**
 * XACML Resource attributes
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public final class ResourceAttributeIds {

	private static final String RESOURCE = "resource:";
	private static final String XACML_URN_RESOURCE_ROOT = JoarkAttributeIds.JOARK_XACML_URN_ROOT + RESOURCE;

	public static final URN RESOURCE_TARGET = resourceUrn("resource-target");
	public static final String ATTR_RESOURCE_TARGET = XACML_URN_RESOURCE_ROOT + "resource-target";

	private static final String JOURNALPOST_AVSENDER_IDENTIFIER = "attr:journalpost:avsenderfnr";
	public static final URN JOURNALPOST_AVSENDER = resourceUrn(JOURNALPOST_AVSENDER_IDENTIFIER);
	public static final String ATTR_JOURNALPOST_AVSENDER = XACML_URN_RESOURCE_ROOT + JOURNALPOST_AVSENDER_IDENTIFIER;

	private static final String ALLE_HISTORISKE_PERSONNUMMER_IDENTIFIER = "person:historiske:fodselsnumre";
	public static final URN ALLE_HISTORISKE_PERSONNUMMER = resourceUrn(ALLE_HISTORISKE_PERSONNUMMER_IDENTIFIER);
	public static final String ATTR_ALLE_HISTORISKE_PERSONNUMMER = XACML_URN_RESOURCE_ROOT
			+ ALLE_HISTORISKE_PERSONNUMMER_IDENTIFIER;

	public static final String DOCUMENT = "dokument";
	public static final String JOURNALPOST = "journalpost";
	public static final String JOURNALPOST_DOCUMENT = "journalpost:document";

	private static URN resourceUrn(String relativeUrn) {
		return new URN(XACML_URN_RESOURCE_ROOT + relativeUrn);
	}

	private ResourceAttributeIds() {
	}
}
