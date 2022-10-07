package no.nav.dokarkiv.core.consumers.saf.graphql;

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
}
