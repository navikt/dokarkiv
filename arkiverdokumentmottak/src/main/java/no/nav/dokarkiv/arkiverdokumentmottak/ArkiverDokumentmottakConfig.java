package no.nav.dokarkiv.arkiverdokumentmottak;


import static org.apache.cxf.ws.security.SecurityConstants.USERNAME_TOKEN_VALIDATOR;
import static org.apache.cxf.ws.security.SecurityConstants.VALIDATE_TOKEN;

import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1.ArkiverDokumentmottakEndpoint;
import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2Endpoint;
import no.nav.dokarkiv.core.security.NavLdapUsernameTokenValidator;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.xml.ws.Endpoint;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration for the ArkiverDokumentMottak ws service
 *
 * @author Stig Strøm
 */
@Configuration
@ComponentScan
public class ArkiverDokumentmottakConfig {

	@Bean
	@Profile("nais")
	public Endpoint arkiverDokumentmottakV1(Bus bus,
											ArkiverDokumentmottakEndpoint arkiverDokumentmottakEndpoint,
											NavLdapUsernameTokenValidator navLdapUsernameTokenValidator) {
		EndpointImpl endpoint = new EndpointImpl(bus, arkiverDokumentmottakEndpoint);
		endpoint.getProperties().put(USERNAME_TOKEN_VALIDATOR, navLdapUsernameTokenValidator);
		endpoint.getProperties().put(VALIDATE_TOKEN, "false");
		endpoint.publish("/arkiverdokumentmottak/v1");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		Map<String, Object> inProps = new HashMap<>();
		inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		inProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		cxfEndpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
		return endpoint;
	}


	@Bean
	@Profile("nais")
	public Endpoint arkiverDokumentmottakV2(Bus bus,
											ArkiverDokumentmottakV2Endpoint arkiverDokumentmottakV2Endpoint,
											NavLdapUsernameTokenValidator navLdapUsernameTokenValidator) {
		EndpointImpl endpoint = new EndpointImpl(bus, arkiverDokumentmottakV2Endpoint);
		endpoint.getProperties().put(USERNAME_TOKEN_VALIDATOR, navLdapUsernameTokenValidator);
		endpoint.getProperties().put(VALIDATE_TOKEN, "false");
		endpoint.publish("/arkiverdokumentmottak/v2");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		Map<String, Object> inProps = new HashMap<>();
		inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		inProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		cxfEndpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
		return endpoint;
	}

}
