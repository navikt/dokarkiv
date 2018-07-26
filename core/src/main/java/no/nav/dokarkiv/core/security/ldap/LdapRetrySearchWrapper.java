package no.nav.dokarkiv.core.security.ldap;

/**
 * @author Tore Gard Andersen.
 */
public class LdapRetrySearchWrapper {
	private String userIdent;
	private LdapLookup ldapLookup;

	public LdapRetrySearchWrapper(String userIdent, LdapLookup ldapLookup) {
		this.userIdent = userIdent;
		this.ldapLookup = ldapLookup;
	}

	public LdapResponse invoke() {
		return ldapLookup.getNAVIdent(userIdent);
	}

}
