package no.nav.dokarkiv.innsynjournal.v2.security;

import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.modig.security.tilgangskontroll.config.AccessControlInterceptorConfig;
import no.nav.modig.security.tilgangskontroll.policy.enrichers.EnvironmentRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.enrichers.SecurityContextRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.pdp.DecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.pdp.picketlink.PicketLinkDecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.pep.EnforcementPoint;
import no.nav.modig.security.tilgangskontroll.policy.pep.PEPImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Import(AccessControlInterceptorConfig.class)
public class LegacyPdpConfig {
	private static final String POLICY_CONFIG_FILE_NAME = "policy-config.xml";

	@Bean
	public EnforcementPoint pep() {
		PEPImpl pep = new PEPImpl(pdp());
		pep.setRequestEnrichers(Arrays.asList(new SecurityContextRequestEnricher(), new EnvironmentRequestEnricher()));
		return pep;
	}

	@Bean
	public DecisionPoint pdp() {
		return new PicketLinkDecisionPoint(getConfigUrl(POLICY_CONFIG_FILE_NAME));
	}

	/*
	 * Workaround Util bean som lar oss autowire locators brukt i Xacml oppsettet.
	 */
	@Bean
	public AutowireUtil autowireUtil(ApplicationContext context) {
		return new AutowireUtil(context.getAutowireCapableBeanFactory());
	}


	private URL getConfigUrl(final String path) {
		try {
			return new ClassPathResource(path).getURL();
		} catch (IOException e) {
			throw new DokarkivTechnicalException("Fant ikke path=" + path + " på classpath", e);
		}
	}
}

