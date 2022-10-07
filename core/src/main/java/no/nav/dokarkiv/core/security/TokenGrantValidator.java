package no.nav.dokarkiv.core.security;

import com.nimbusds.jwt.JWTParser;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.ParseException;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class TokenGrantValidator {

	private static final String OID_CLAIM_NAME = "oid";

	public void validateOnBehalfOfAccessToken(String authHeader) {
		try {
			var token = StringUtils.split(authHeader, " ")[1];
			var jwtToken = JWTParser.parse(token);
			var oid = jwtToken.getJWTClaimsSet().getStringClaim(OID_CLAIM_NAME);
			var sub = jwtToken.getJWTClaimsSet().getSubject();

			if (isBlank(oid)) {
				throw new InputValideringFeiletException("Access Token mangler OID claim");
			}
			if (isBlank(sub)) {
				throw new InputValideringFeiletException("Access Token mangler Subject claim");
			}
			if (StringUtils.equals(oid, sub)) {
				throw new InputValideringFeiletException("Access Token er ikke et On-Behalf-Of token");
			}
		} catch (ParseException e) {
			throw new RuntimeException(String.format("En feil oppsto ved parsing av Access Token. Feilmelding=%s", e.getMessage()), e);
		}
	}
}
