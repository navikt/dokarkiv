package no.nav.dokarkiv.arkiverdokumentmottak;


import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1.ArkiverDokumentmottakEndpoint;
import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2Endpoint;
import no.nav.dokarkiv.core.security.LdapUsernameTokenValidatorInterceptor;
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
	public Endpoint arkiverDokumentmottakV1(Bus bus, LdapUsernameTokenValidatorInterceptor ldapUsernameTokenValidatorInterceptor, ArkiverDokumentmottakEndpoint arkiverDokumentmottakEndpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, arkiverDokumentmottakEndpoint);
		endpoint.publish("/arkiverdokumentmottak/v1");
		endpoint.getProperties().put("ws-security.validate.token", "false");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		Map<String, Object> inProps = new HashMap<>();
		inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		inProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		cxfEndpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
		cxfEndpoint.getInInterceptors().add(ldapUsernameTokenValidatorInterceptor);
		return endpoint;
	}


	@Bean
	@Profile("nais")
	public Endpoint arkiverDokumentmottakV2(Bus bus, LdapUsernameTokenValidatorInterceptor ldapUsernameTokenValidatorInterceptor, ArkiverDokumentmottakV2Endpoint arkiverDokumentmottakV2Endpoint) {
		EndpointImpl endpoint = new EndpointImpl(bus, arkiverDokumentmottakV2Endpoint);
		endpoint.publish("/arkiverdokumentmottak/v2");
		endpoint.getProperties().put("ws-security.validate.token", "false");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		Map<String, Object> inProps = new HashMap<>();
		inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		inProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		cxfEndpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
		cxfEndpoint.getInInterceptors().add(ldapUsernameTokenValidatorInterceptor);
		return endpoint;
	}

}
