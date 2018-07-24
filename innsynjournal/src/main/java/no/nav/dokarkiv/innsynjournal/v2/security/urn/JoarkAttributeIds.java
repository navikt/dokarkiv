package no.nav.dokarkiv.innsynjournal.v2.security.urn;

import no.nav.modig.security.tilgangskontroll.policy.attributes.AttributeIds;

/**
 * Base URI for joark related XACML URIs
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public final class JoarkAttributeIds {

	public static final String JOARK_XACML_URN_ROOT = AttributeIds.NAV_XACML_URN_ROOT + "joark:";

	private JoarkAttributeIds() {
	}
}
