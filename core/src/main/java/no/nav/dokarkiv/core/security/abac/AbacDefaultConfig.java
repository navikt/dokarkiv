package no.nav.dokarkiv.core.security.abac;

import no.nav.abac.xacml.NavAttributter;
import no.nav.freg.abac.core.annotation.attribute.AbacAttributeLocator;
import no.nav.freg.abac.core.annotation.attribute.ResolvingAbacAttributeLocator;
import no.nav.freg.security.oidc.auth.common.OidcTokenAuthentication;
import no.nav.modig.core.context.SubjectHandler;
import org.apache.wss4j.common.util.DOM2Writer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class AbacDefaultConfig {

	public static final String ENVIRONMENT_FELLES_CONSUMER_OIDC_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.consumer_oidc_token_body";

	@Bean
	Set<String> abacDefaultEnvironment() {
		Set<String> values = new HashSet<>();
		values.add(NavAttributter.ENVIRONMENT_FELLES_PEP_ID);
		values.add(NavAttributter.ENVIRONMENT_FELLES_SAML_TOKEN);
		values.add(NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY);
		values.add(ENVIRONMENT_FELLES_CONSUMER_OIDC_TOKEN_BODY);
		return values;
	}

	@Bean
	Set<String> abacDefaultResources() {
		Set<String> values = new HashSet<>();
		values.add(NavAttributter.RESOURCE_FELLES_DOMENE);
		return values;
	}

	@Bean
	Set<String> abacDefaultSubjects() {
		return new HashSet<>();
	}

	@Bean
	Set<String> abacDefaultActions() {
		return new HashSet<>();
	}

	@Bean
	AbacAttributeLocator pepIdLocator() {
		return new ResolvingAbacAttributeLocator(NavAttributter.ENVIRONMENT_FELLES_PEP_ID, () -> JoarkAbacAttributes.PEP_ID);
	}

	@Bean
	AbacAttributeLocator fellesDomeneLocator() {
		return new ResolvingAbacAttributeLocator(NavAttributter.RESOURCE_FELLES_DOMENE, () -> JoarkAbacAttributes.ARKIV);
	}

	@Bean
	AbacAttributeLocator resourceTypeLocator() {
		return new ResolvingAbacAttributeLocator(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, () -> NavAttributter.RESOURCE_ARKIV_JOURNALPOST);
	}

	@Bean
	AbacAttributeLocator samlTokenLocator() {
		return new ResolvingAbacAttributeLocator(NavAttributter.ENVIRONMENT_FELLES_SAML_TOKEN, () -> {
			if (SubjectHandler.getSubjectHandler().getSAMLAssertion() == null) {
				return null;
			} else {
				return DOM2Writer.nodeToString(SubjectHandler.getSubjectHandler().getSAMLAssertion()).getBytes();
			}
		});
	}

	@Bean
	AbacAttributeLocator authorizationHeaderOidcTokenLocator() {
		return new ResolvingAbacAttributeLocator(NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, () -> {
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				return null;
			} else {
				return ((OidcTokenAuthentication) SecurityContextHolder.getContext().getAuthentication()).getIdTokenBody();
			}
		});
	}

	@Bean
	AbacAttributeLocator navConsumerHeaderOidcTokenLocator() {
		return new ResolvingAbacAttributeLocator(ENVIRONMENT_FELLES_CONSUMER_OIDC_TOKEN_BODY, () -> {
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				return null;
			} else {
				return ((OidcTokenAuthentication) SecurityContextHolder.getContext()
						.getAuthentication()).getConsumerTokenBody();
			}
		});
	}
}
