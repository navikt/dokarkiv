package no.nav.dokarkiv.logiskslettdokument.security;

import static no.nav.dokarkiv.logiskslettdokument.security.SecurityConfig.AZURE_ISSUER_URL;
import static no.nav.dokarkiv.logiskslettdokument.security.SecurityConfig.GOOGLE_ISSUER_URL;
import static no.nav.dokarkiv.logiskslettdokument.security.SecurityConfig.NAV_STS_ISSUER_URL;
import static no.nav.dokarkiv.logiskslettdokument.security.SecurityConfig.OPEN_AM_ISSUER_URL;

import lombok.experimental.UtilityClass;
import no.nav.freg.security.test.oidc.tools.JwtClaimsBuilder;
import org.jose4j.jwt.JwtClaims;

import java.time.LocalDateTime;

@UtilityClass
public class JwtClaimsBuilderProvider {

	public static JwtClaimsBuilder defaultClaimsBuilder() {
		return new JwtClaimsBuilder()
				.subject("sub")
				.audience("aud")
				.expiry(LocalDateTime.now().plusMinutes(10))
				.validFrom(LocalDateTime.now().minusMinutes(5))
				.azp("azp");
	}

	public static JwtClaimsBuilder openAmClaimsBuilder() {
		return defaultClaimsBuilder().issuer(OPEN_AM_ISSUER_URL);
	}

	public static JwtClaims openAmClaims() {
		return openAmClaimsBuilder().build();
	}

	public static JwtClaimsBuilder navStsClaimsBuilder() {
		return defaultClaimsBuilder().issuer(NAV_STS_ISSUER_URL);
	}

	public static JwtClaims navStsClaims() {
		return navStsClaimsBuilder().build();
	}

	public static JwtClaimsBuilder googleClaimsBuilder() {
		return defaultClaimsBuilder().issuer(GOOGLE_ISSUER_URL);
	}

	public static JwtClaimsBuilder azureClaimsBuilder() {
		return defaultClaimsBuilder()
				.issuer(AZURE_ISSUER_URL)
				.azp(null);
	}

	public static JwtClaims googleClaims() {
		return googleClaimsBuilder().build();
	}
}