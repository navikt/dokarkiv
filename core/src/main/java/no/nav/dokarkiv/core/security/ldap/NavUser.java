package no.nav.dokarkiv.core.security.ldap;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"displayName", "description"})
@Entry(objectClasses = {"organizationalPerson"})
public final class NavUser {
	@Id
	private Name dn;

	@Attribute(name = "cn")
	private String userId;
	private String displayName;
	private String description;
	private Set<String> memberOf;

	/**
	 * Flagg som forteller hvorvidt brukeren ble funnet i ldap
	 **/
	private boolean userExistsInLdap = true;

	public String getFullname() {
		if (isNotBlank(description)) {
			return description;
		}
		if (isNotBlank(displayName)) {
			return displayName;
		}
		//fallback
		return userId;
	}
}



