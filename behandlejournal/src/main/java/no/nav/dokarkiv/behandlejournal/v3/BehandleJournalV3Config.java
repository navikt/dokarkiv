package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.behandlejournal.BehandleJournalSporingMapper;
import no.nav.dokarkiv.core.security.ValidateSamlInInterceptor;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.xml.ws.Endpoint;

@Import(BehandleJournalSporingMapper.class)
@Configuration
@ComponentScan
public class BehandleJournalV3Config {

	@Bean
	@Profile("nais")
	Endpoint behandleJournalv3(Bus bus,
							   BehandleJournalV3 behandleJournalV3Endpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, behandleJournalV3Endpoint);
		endpoint.publish("/behandlejournal/v3");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		cxfEndpoint.getInInterceptors().add(new ValidateSamlInInterceptor());
		return endpoint;
	}
}
