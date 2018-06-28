package no.nav.dokarkiv.arkiverdokumentmottak;


import no.nav.dokarkiv.arkiverdokumentmottak.v1.ArkiverDokumentmottakEndpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.xml.ws.Endpoint;

/**
 * Spring configuration for the ArkiverDokumentMottakV1 ws service
 *
 * @author Stig Strøm
 */
@Configuration
@ComponentScan
public class ArkiverDokumentmottakConfig {

	@Bean
	@Profile("nais")
	Endpoint arkiverDokumentmottakV1(Bus bus, ArkiverDokumentmottakEndpoint arkiverDokumentmottakEndpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, arkiverDokumentmottakEndpoint);
		endpoint.publish("/arkiverdokumentmottak/v1");
		return endpoint;
	}

}
