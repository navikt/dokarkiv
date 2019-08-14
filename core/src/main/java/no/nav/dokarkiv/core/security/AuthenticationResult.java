//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package no.nav.dokarkiv.core.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationResult {
	private final String user;
	private final String consumerId;
	private final boolean isValid;
	private final String errorMessage;

	public static AuthenticationResult invalid(String errorMessage) {
		return new AuthenticationResult((String) null, (String) null, false, errorMessage);
	}

	public static AuthenticationResult success(String user, String consumerId) {
		return new AuthenticationResult(user, consumerId, true, (String) null);
	}

	public boolean isValid() {
		return this.isValid;
	}

	public String getUser() {
		return this.user;
	}

	public String getConsumerId() {
		return this.consumerId;
	}

	public String getErrorMessage() {
		if (this.isValid) {
			throw new IllegalArgumentException("Can't get error message from valid token");
		} else {
			return this.errorMessage;
		}
	}

	public String toString() {
		return (new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).append("user", this.user).append("consumerId", this.consumerId).append("isValid", this.isValid).append("errorMessage", this.errorMessage).toString();
	}
}
