package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Profile("itest")
public class STSTestConfig extends STSConfig {

	public STSTestConfig(@Value("${securityTokenService.url}") String stsUrl, ServiceuserAlias serviceuserAlias) {
		super(stsUrl, serviceuserAlias);
	}

	@Override
	public void configureSTS(Object port){

	}
	
}
