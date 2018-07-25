package no.nav.dokarkiv.core.security.abac;

import no.nav.abac.xacml.NavAttributter;
import no.nav.freg.abac.core.annotation.attribute.AbacAttributeLocator;
import no.nav.freg.abac.core.annotation.attribute.ResolvingAbacAttributeLocator;
import no.nav.modig.core.context.SubjectHandler;
import org.apache.wss4j.common.util.DOM2Writer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class AbacDefaultConfig {

	@Bean
	Set<String> abacDefaultEnvironment() {
		Set<String> values = new HashSet<>();
		values.add(NavAttributter.ENVIRONMENT_FELLES_PEP_ID);
		values.add(NavAttributter.ENVIRONMENT_FELLES_SAML_TOKEN);
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
				return new byte[]{};
			} else {
				return DOM2Writer.nodeToString(SubjectHandler.getSubjectHandler().getSAMLAssertion()).getBytes();
			}
		});
	}
}
