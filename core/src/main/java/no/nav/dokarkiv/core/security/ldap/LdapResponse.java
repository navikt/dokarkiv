package no.nav.dokarkiv.core.security.ldap;

/**
 * Ldap response that may contain null
 *
 * @author Andreas Skomedal, Visma Consulting
 */
public class LdapResponse {
	private String ident;
	private String response;

	public LdapResponse(String ident, String response) {
		this.ident = ident;
		this.response = response;
	}

	/**
	 * Get response, or null if no response found
	 *
	 * @return the response
	 */
	public String orNull() {
		return response;
	}

	/**
	 * Get the response, or ident if no response found
	 *
	 * @return the response or ident
	 */
	public String orIdent() {
		return response != null ? response : ident;
	}

	/**
	 * Get the response, or the given name if no response found
	 *
	 * @param name the desired name to use
	 * @return the response or name
	 */
	public String or(String name) {
		return response != null ? response : name;
	}
}
