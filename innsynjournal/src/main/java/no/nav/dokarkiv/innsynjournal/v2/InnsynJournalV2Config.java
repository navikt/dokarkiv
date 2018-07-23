package no.nav.dokarkiv.innsynjournal.v2;

import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.security.ValidateSamlInInterceptor;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2;
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
public class InnsynJournalV2Config {
	@Bean
	@Profile("nais")
	Endpoint innsynJournalV2(Bus bus,
							   InnsynJournalV2 innsynJournalV2Endpoint) {
		System.setProperty(ThreadLocalSubjectHandler.SUBJECTHANDLER_KEY, ThreadLocalSubjectHandler.class.getName());
		EndpointImpl endpoint = new EndpointImpl(bus, innsynJournalV2Endpoint);
		endpoint.publish("/innsynjournal/v2");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		cxfEndpoint.getInInterceptors().add(new ValidateSamlInInterceptor());
		return endpoint;
	}
}
