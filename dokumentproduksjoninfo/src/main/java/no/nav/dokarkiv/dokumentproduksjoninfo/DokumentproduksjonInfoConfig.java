package no.nav.dokarkiv.dokumentproduksjoninfo;

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
public class DokumentproduksjonInfoConfig {

	@Bean
	@Profile("nais")
	Endpoint dokumentproduksjonInfoV1(Bus bus, DokumentproduksjonInfoEndpoint dokumentproduksjonInfoEndpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, dokumentproduksjonInfoEndpoint);
		endpoint.publish("/dokumentproduksjoninfo/v1");
		return endpoint;
	}
}
