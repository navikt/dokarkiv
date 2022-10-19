package no.nav.dokarkiv.core.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.ConsumerUnauthorizedDokarkivFunctionalException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.ParseException;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class TokenGrantValidator {

	private static final String OID_CLAIM_NAME = "oid";

	public JWTClaimsSet validateOnBehalfOfAccessToken(String authHeader) {
		try {
			var token = StringUtils.split(authHeader, " ")[1];
			var jwtToken = JWTParser.parse(token);
			var claimsSet = jwtToken.getJWTClaimsSet();
			var oid = claimsSet.getStringClaim(OID_CLAIM_NAME);
			var sub = claimsSet.getSubject();

			if (isBlank(oid)) {
				throw new ConsumerUnauthorizedDokarkivFunctionalException(
						"Access token på Authorization header mangler oid claim. " +
								"Dette betyr at vi ikke gjenkjenner token som utstedt av NAV sin Azure tenant. " +
								"Vennligst forsøk på nytt med On-Behalf-Of flow access token fra NAV sin Azure tenant. " +
								"Hvis ikke dette fungerer, kontakt oss på #team_dokumentløsninger");
			}
			if (isBlank(sub)) {
				throw new ConsumerUnauthorizedDokarkivFunctionalException(
						"Access token på Authorization header mangler subject claim. " +
								"Dette betyr at vi ikke kan se hvem token er utstedt til av NAV sin Azure tenant. " +
								"Vennligst forsøk på nytt med On-Behalf-Of flow access token fra NAV sin Azure tenant. " +
								"Hvis ikke dette fungerer, kontakt oss på #team_dokumentløsninger");
			}
			if (StringUtils.equals(oid, sub)) {
				throw new ConsumerUnauthorizedDokarkivFunctionalException(
						"Access token på Authorization header er ikke er On-Behalf-Of-token" +
								"Dette betyr at vi ikke gjenkjenner tokenet som riktig type token utstedt av NAV sin Azure tenant. " +
								"Vennligst forsøk på nytt med On-Behalf-Of flow access token fra NAV sin Azure tenant. " +
								"Hvis ikke dette fungerer, kontakt oss på #team_dokumentløsninger");
			}
			return claimsSet;
		} catch (ParseException e) {
			log.warn("En feil oppsto ved parsing av Access Token. Feilmelding={}", e.getMessage(), e);
			throw new ConsumerUnauthorizedDokarkivFunctionalException(
					"Kunne ikke parse Access token på Authorization header. " +
							"Dette betyr at vi ikke gjenkjenner dette token som et gyldig token utstedt av NAV sin Azure tenant. " +
							"Vennligst forsøk på nytt med On-Behalf-Of flow access token fra NAV sin Azure tenant. " +
							"Hvis ikke dette fungerer, kontakt oss på #team_dokumentløsninger");
		}
	}
}
