package no.nav.dokarkiv.core.security.ldap;

/**
 * Ldap lookup interface
 *
 * @author Andreas Berg Skomedal, Visma Consulting.
 */
public interface LdapLookup {

	/**
	 * Search for service user in Ldap and get their name
	 *
	 * @param userIdent the user ident
	 * @return LdapResponse
	 */
	LdapResponse getServiceUserName(String userIdent);

	/**
	 * Search for person user in Ldap and get their name
	 *
	 * @param userIdent the user ident
	 * @return LdapResponse
	 */
	LdapResponse getNAVIdent(String userIdent);

	/**
	 * Get number of missed ldap lookups,
	 * may count a user several times if the lookup happened outside the cachelimit
	 * or if caching is disabled
	 *
	 * @return the number of misses
	 */
	long getLookups();

	/**
	 * Get number of lookups to ldap that returned no result
	 *
	 * @return the number of lookups missed
	 */
	long getMisses();

	/**
	 * Get number of milliseconds spent on lookup
	 *
	 * @return the number of milliseconds
	 */
	long getTime();

	/**
	 * Clear cache and counters
	 */
	void clear();

}
