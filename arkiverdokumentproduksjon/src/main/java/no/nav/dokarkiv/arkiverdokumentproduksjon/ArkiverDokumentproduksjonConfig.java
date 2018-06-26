package no.nav.dokarkiv.arkiverdokumentproduksjon;

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
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@ComponentScan
public class ArkiverDokumentproduksjonConfig {

	@Bean
	@Profile("nais")
	Endpoint arkiverDokumentproduksjon(Bus bus,
									   ArkiverDokumentproduksjonEndpoint arkiverDokumentproduksjonEndpoint,
									   LdapUsernameTokenValidatorInterceptor ldapUsernameTokenValidatorInterceptor) {
		EndpointImpl endpoint = new EndpointImpl(bus, arkiverDokumentproduksjonEndpoint);
		endpoint.publish("/arkiverdokumentproduksjon/v1");
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = endpoint.getServer().getEndpoint();
		endpoint.getProperties().put("ws-security.validate.token", "false");
		Map<String, Object> inProps = new HashMap<>();
		inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		inProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		cxfEndpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
		cxfEndpoint.getInInterceptors().add(ldapUsernameTokenValidatorInterceptor);
		return endpoint;
	}
}
