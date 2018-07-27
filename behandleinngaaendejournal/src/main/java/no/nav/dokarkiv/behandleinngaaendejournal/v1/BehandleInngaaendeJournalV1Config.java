package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import no.nav.dokarkiv.core.security.ValidateSamlInInterceptor;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
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
public class BehandleInngaaendeJournalV1Config {
	@Bean
	@Profile("nais")
	Endpoint behandleInngaaendeJournalV1(Bus bus,
										 BehandleInngaaendeJournalV1 behandleInngaaendeJournalEndpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, behandleInngaaendeJournalEndpoint);
		endpoint.publish("/behandleinngaaendejournal/v1");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		cxfEndpoint.getInInterceptors().add(new ValidateSamlInInterceptor());
		return endpoint;
	}
}
