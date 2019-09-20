package no.nav.dokarkiv.arkiverdokumentproduksjon;

import static org.apache.cxf.ws.security.SecurityConstants.USERNAME_TOKEN_VALIDATOR;
import static org.apache.cxf.ws.security.SecurityConstants.VALIDATE_TOKEN;

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
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@ComponentScan
public class ArkiverDokumentproduksjonConfig {

	@Bean
	@Profile("nais")
	Endpoint arkiverDokumentproduksjon(Bus bus,
									   ArkiverDokumentproduksjonEndpoint arkiverDokumentproduksjonEndpoint,
									   NavLdapUsernameTokenValidator navLdapUsernameTokenValidator) {
		EndpointImpl endpoint = new EndpointImpl(bus, arkiverDokumentproduksjonEndpoint);
		endpoint.getProperties().put(USERNAME_TOKEN_VALIDATOR, navLdapUsernameTokenValidator);
		endpoint.getProperties().put(VALIDATE_TOKEN, "false");
		endpoint.publish("/arkiverdokumentproduksjon/v1");
		final org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		Map<String, Object> inProps = new HashMap<>();
		inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		inProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		cxfEndpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
		return endpoint;
	}
}
