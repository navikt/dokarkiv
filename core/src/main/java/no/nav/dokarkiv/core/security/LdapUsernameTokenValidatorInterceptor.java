package no.nav.dokarkiv.core.security;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.interceptor.security.AbstractUsernameTokenInInterceptor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.security.auth.Subject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class LdapUsernameTokenValidatorInterceptor extends AbstractUsernameTokenInInterceptor {

	private final LdapTemplate ldapTemplate;

	@Inject
	public LdapUsernameTokenValidatorInterceptor(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	@Override
	protected Subject createSubject(UsernameToken token) {
		try {
			ldapTemplate.authenticate(query().where("cn").is(token.getName()), token.getPassword());
		} catch(Exception e) {
			reportSecurityException("Innlogging med bruker " + token.getName() + " feilet.");
		}
		Subject subject = new Subject();
		subject.getPrincipals().add(new SimplePrincipal(token.getName()));
		return subject;
	}
}
