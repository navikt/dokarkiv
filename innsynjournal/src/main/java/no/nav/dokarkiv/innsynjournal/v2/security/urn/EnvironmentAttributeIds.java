package no.nav.dokarkiv.innsynjournal.v2.security.urn;

import no.nav.modig.security.tilgangskontroll.URN;

/**
 * XACML Environment attributes
 *
 * NB! Det er bug i modig-security som gjør at alle environment-attributter må settes som en annen type.
 * (Se http://jira.adeo.no/browse/PKFEIL-15603)
 * Feilen gjør at ingen environment-attributter blir satt på XACML-requesten ved evaluering.
 * Denne feilen ligger i {@link no.nav.modig.security.tilgangskontroll.policy.pdp.picketlink.RequestTypeFactory}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Deprecated
public final class EnvironmentAttributeIds {

	private static final String ENVIRONMENT = "environment:";
	private static final String XACML_URN_ENVIRONMENT_ROOT = JoarkAttributeIds.JOARK_XACML_URN_ROOT + ENVIRONMENT;

	private static final String ENVIRONMENT_RECIEVER_STR = "environment-receiver";
	public static final URN ENVIRONMENT_RECIEVER = environmentUrn(ENVIRONMENT_RECIEVER_STR);
	public static final String ATTR_ENVIRONMENT_RECIEVER = XACML_URN_ENVIRONMENT_ROOT + ENVIRONMENT_RECIEVER_STR;

	public static final String INTERNAL = "internal";
	public static final String EXTERNAL = "external";

	private static URN environmentUrn(String relativeUrn) {
		return new URN(XACML_URN_ENVIRONMENT_ROOT + relativeUrn);
	}

}
