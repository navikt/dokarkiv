package no.nav.dokarkiv.inngaaendejournal.v1;

import no.nav.dokarkiv.core.security.ValidateSamlInInterceptor;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;
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
public class InngaaendeJournalV1Config {
	@Bean
	@Profile("nais")
	Endpoint inngaaendeJournalV1(Bus bus,
							 InngaaendeJournalV1 inngaaendeJournalEndpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, inngaaendeJournalEndpoint);
		endpoint.publish("/inngaaendejournal/v1");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		cxfEndpoint.getInInterceptors().add(new ValidateSamlInInterceptor());
		return endpoint;
	}
}
