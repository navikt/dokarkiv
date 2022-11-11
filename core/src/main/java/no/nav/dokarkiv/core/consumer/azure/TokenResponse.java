package no.nav.dokarkiv.core.consumer.azure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
	private String access_token;
	private String token_type;
	private String expires_in;
	private String error;
	private String ext_expires_in;
}
