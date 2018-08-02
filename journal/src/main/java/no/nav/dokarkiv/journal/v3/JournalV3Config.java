package no.nav.dokarkiv.journal.v3;

import no.nav.dokarkiv.core.security.ValidateSamlInInterceptor;
import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.xml.ws.Endpoint;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@ComponentScan
public class JournalV3Config {
	@Bean
	@Profile("nais")
	Endpoint journalV3(Bus bus,
					   JournalV3 journalV3Endpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, journalV3Endpoint);
		endpoint.publish("/journal/v3");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		cxfEndpoint.getInInterceptors().add(new ValidateSamlInInterceptor());
		return endpoint;
	}
}
