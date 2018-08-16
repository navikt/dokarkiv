package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.behandlejournal.BehandleJournalSporingMapper;
import no.nav.dokarkiv.core.security.ValidateSamlInInterceptor;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.BehandleJournalV2;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.xml.ws.Endpoint;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Import(BehandleJournalSporingMapper.class)
@Configuration
@ComponentScan
public class BehandleJournalV2Config {

	@Bean
	@Profile("nais")
	Endpoint behandleJournalv2(Bus bus,
							   BehandleJournalV2 behandleJournalEndpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, behandleJournalEndpoint);
		endpoint.publish("/behandlejournal/v2");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		cxfEndpoint.getInInterceptors().add(new ValidateSamlInInterceptor());
		return endpoint;
	}
}
