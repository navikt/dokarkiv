package no.nav.dokarkiv.core.security.ldap;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

/**
 * Maps Ldap Attributes to name
 *
 * @author Andreas Berg Skomedal, Visma Consulting.
 */
public class NameMapper implements AttributesMapper<String> {

	public static final String DESCRIPTION = "description";
	public static final String DISPLAYNAME = "displayname";

	@Override
	public String mapFromAttributes(Attributes attributes) throws NamingException {
		// Description contains most consistent naming format
		Attribute description = attributes.get(DESCRIPTION);
		if (description != null) {
			return (String) description.get();
		}
		Attribute dname = attributes.get(DISPLAYNAME);
		if (dname != null) {
			return (String) dname.get();
		}
		return null;
	}

}
