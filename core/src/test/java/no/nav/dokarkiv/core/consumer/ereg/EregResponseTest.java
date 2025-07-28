package no.nav.dokarkiv.core.consumer.ereg;

import no.nav.dokarkiv.core.consumer.ereg.EregResponse.Navn.Bruksperiode;
import no.nav.dokarkiv.core.consumer.ereg.EregResponse.Navn.Gyldighetsperiode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.dokarkiv.core.util.TestDataUtils.FORTID;
import static no.nav.dokarkiv.core.util.TestDataUtils.FORTID_DATO;
import static no.nav.dokarkiv.core.util.TestDataUtils.FREMTID;
import static no.nav.dokarkiv.core.util.TestDataUtils.FREMTID_DATO;
import static org.assertj.core.api.Assertions.assertThat;

public class EregResponseTest {

	private static final String ORGANISASJONSNUMMER = "123456789";
	private static final String SAMMENSATT_NAVN = "Sammensatt Navn";

	private static final Bruksperiode GYLDIG_BRUKSPERIODE = new Bruksperiode(FORTID, FREMTID);
	private static final Bruksperiode GYLDIG_BRUKSPERIODE_2 = new Bruksperiode(FORTID, null);
	private static final Bruksperiode UGYLDIG_BRUKSPERIODE = new Bruksperiode(FREMTID, FORTID);
	private static final Bruksperiode UGYLDIG_BRUKSPERIODE_2 = new Bruksperiode(null, FREMTID);
	private static final Bruksperiode UGYLDIG_BRUKSPERIODE_3 = new Bruksperiode(null, null);

	private static final Gyldighetsperiode GYLDIG_GYLDIGHETSPERIODE = new Gyldighetsperiode(FORTID_DATO, FREMTID_DATO);
	private static final Gyldighetsperiode GYLDIG_GYLDIGHETSPERIODE_2 = new Gyldighetsperiode(FORTID_DATO, null);
	private static final Gyldighetsperiode UGYLDIG_GYLDIGHETSPERIODE = new Gyldighetsperiode(FREMTID_DATO, FORTID_DATO);
	private static final Gyldighetsperiode UGYLDIG_GYLDIGHETSPERIODE_2 = new Gyldighetsperiode(null, FREMTID_DATO);
	private static final Gyldighetsperiode UGYLDIG_GYLDIGHETSPERIODE_3 = new Gyldighetsperiode(null, null);

	@ParameterizedTest
	@MethodSource
	public void shouldReturnValid(Bruksperiode bruksperiode, Gyldighetsperiode gyldighetsperiode) {
		var response = new EregResponse(
				ORGANISASJONSNUMMER,
				new EregResponse.Navn(
						SAMMENSATT_NAVN,
						bruksperiode,
						gyldighetsperiode)
		);

		assertThat(response.navn().erGyldig()).isTrue();
	}

	public static Stream<Arguments> shouldReturnValid() {
		return Stream.of(
				Arguments.of(GYLDIG_BRUKSPERIODE, GYLDIG_GYLDIGHETSPERIODE),
				Arguments.of(GYLDIG_BRUKSPERIODE, GYLDIG_GYLDIGHETSPERIODE_2),
				Arguments.of(GYLDIG_BRUKSPERIODE_2, GYLDIG_GYLDIGHETSPERIODE),
				Arguments.of(GYLDIG_BRUKSPERIODE_2, GYLDIG_GYLDIGHETSPERIODE_2));
	}

	@ParameterizedTest
	@MethodSource
	public void shouldReturnInvalid(Bruksperiode bruksperiode, Gyldighetsperiode gyldighetsperiode) {
		var response = new EregResponse(
				ORGANISASJONSNUMMER,
				new EregResponse.Navn(
						SAMMENSATT_NAVN,
						bruksperiode,
						gyldighetsperiode)
		);

		assertThat(response.navn().erGyldig()).isFalse();
	}

	public static Stream<Arguments> shouldReturnInvalid() {
		return Stream.of(
				Arguments.of(GYLDIG_BRUKSPERIODE, UGYLDIG_GYLDIGHETSPERIODE),
				Arguments.of(GYLDIG_BRUKSPERIODE, UGYLDIG_GYLDIGHETSPERIODE_2),
				Arguments.of(GYLDIG_BRUKSPERIODE, UGYLDIG_GYLDIGHETSPERIODE_3),
				Arguments.of(UGYLDIG_BRUKSPERIODE, GYLDIG_GYLDIGHETSPERIODE),
				Arguments.of(UGYLDIG_BRUKSPERIODE_2, GYLDIG_GYLDIGHETSPERIODE),
				Arguments.of(UGYLDIG_BRUKSPERIODE_3, GYLDIG_GYLDIGHETSPERIODE),
				Arguments.of(null, null),
				Arguments.of(null, GYLDIG_GYLDIGHETSPERIODE),
				Arguments.of(GYLDIG_BRUKSPERIODE, null));
	}
}