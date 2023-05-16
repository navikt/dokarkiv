package no.nav.dokarkiv.core.security.abac;

import lombok.extern.slf4j.Slf4j;
import no.nav.abac.xacml.NavAttributter;
import no.nav.abac.xacml.StandardAttributter;
import no.nav.dokarkiv.core.NavHeaders;
import no.nav.freg.abac.core.annotation.attribute.AbacAttributeLocator;
import no.nav.freg.abac.core.annotation.attribute.ResolvingAbacAttributeLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
public class AbacDefaultConfig {

	@Bean
	Set<String> abacDefaultEnvironment() {
		Set<String> values = new HashSet<>();
		values.add(NavAttributter.ENVIRONMENT_FELLES_PEP_ID);
		values.add(NavAttributter.ENVIRONMENT_FELLES_SAML_TOKEN);
		values.add(NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY);
		values.add(NavAttributter.ENVIRONMENT_FELLES_CONSUMER_OIDC_TOKEN_BODY);
		values.add(StandardAttributter.SUBJECT_ID);
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
		Set<String> values = new HashSet<>();
		values.add(StandardAttributter.SUBJECT_ID);
		values.add(NavAttributter.SUBJECT_FELLES_SUBJECTTYPE);
		return values;
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
	AbacAttributeLocator authorizationHeaderOidcTokenLocator() {
		return new ResolvingAbacAttributeLocator(NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, () -> headerLocator(HttpHeaders.AUTHORIZATION));
	}

	@Bean
	AbacAttributeLocator navConsumerHeaderOidcTokenLocator() {
		return new ResolvingAbacAttributeLocator(NavAttributter.ENVIRONMENT_FELLES_CONSUMER_OIDC_TOKEN_BODY, () -> headerLocator(NavHeaders.NAV_CONSUMER_TOKEN));
	}

	private String headerLocator(final String header) {
		final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			final HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			final String headerValue = request.getHeader(header);
			if (headerValue == null) {
				return null;
			}
			// Fant JWT token og returnerer payload delen.
			if (headerValue.startsWith(NavHeaders.BEARER_TOKEN_PREFIX)) {
				return headerValue.split("\\.")[1];
			}
		}
		return null;
	}

	@Bean
	AbacAttributeLocator basicAuthHeaderLocator() {
		return new ResolvingAbacAttributeLocator(StandardAttributter.SUBJECT_ID, () -> {
			if (SecurityContextHolder.getContext().getAuthentication() == null || !(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken)) {
				return null;
			} else {
				return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			}
		});
	}

	@Bean
	AbacAttributeLocator basicAuthHeaderSystemressursLocator() {
		return new ResolvingAbacAttributeLocator(NavAttributter.SUBJECT_FELLES_SUBJECTTYPE, () -> {
			if (SecurityContextHolder.getContext().getAuthentication() == null || !(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken)) {
				return null;
			} else {
				return "Systemressurs";
			}
		});
	}
}
