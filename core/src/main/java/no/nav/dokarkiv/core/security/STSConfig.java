package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.fasit.ServiceuserAlias;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Profile("nais")
public class STSConfig {

	private final String stsUrl;
	private final ServiceuserAlias serviceuserAlias;

	@Inject
	public STSConfig(@Value("${securityTokenService.url}") String stsUrl, ServiceuserAlias serviceuserAlias) {
		this.stsUrl = stsUrl;
		this.serviceuserAlias = serviceuserAlias;
	}

	public void configureSTS(Object port){
		Client client = ClientProxy.getClient(port);
		STSConfigUtil.configureStsRequestSamlToken(client, stsUrl, serviceuserAlias.getUsername(), serviceuserAlias.getPassword());
	}
}
