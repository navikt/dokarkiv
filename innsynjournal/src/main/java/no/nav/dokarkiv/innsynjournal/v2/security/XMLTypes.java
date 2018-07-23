package no.nav.dokarkiv.innsynjournal.v2.security;

import java.net.URI;

/**
 * The class contains constants that specifies XML-types
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public final class XMLTypes {

	private static final String STRING_TYPE_IDENTIFIER = "http://www.w3.org/2001/XMLSchema#string";
	public static final URI STRING_TYPE_URI = URI.create(STRING_TYPE_IDENTIFIER);

	private XMLTypes() {

	}
}
