package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsluttAlleSakerPaaTemaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Stream;

import static no.nav.dokarkiv.journalpost.v1.validators.AvsluttAlleSakerPaaTemaValidator.validerAvsluttAlleSakerPaaTemaRequest;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class AvsluttAlleSakerPaaTemaValidatorTest {

	private static final String TEMA = "FAR";
	private static final String REFERANSE = "MMA-7899";
	private static final LocalDateTime AVSLUTTET_DATO = LocalDateTime.now();
	private static final String ADMINISTRATIV_ENHET = "1294";

	@ParameterizedTest
	@MethodSource
	void skalValidere(String tema, String referanse, LocalDateTime avsluttetDato, String administrativEnhet) {
		var request = new AvsluttAlleSakerPaaTemaRequest(tema, referanse, avsluttetDato, administrativEnhet);

		assertThatNoException().isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(request));
	}

	public static Stream<Arguments> skalValidere() {
		return Stream.of(
				Arguments.of(TEMA, REFERANSE, AVSLUTTET_DATO, ADMINISTRATIV_ENHET),
				Arguments.of(TEMA, REFERANSE, null, ADMINISTRATIV_ENHET),
				Arguments.of(TEMA, REFERANSE, AVSLUTTET_DATO, null),
				Arguments.of(TEMA, REFERANSE, null, null)
		);
	}

	@ParameterizedTest
	@MethodSource
	void skalKasteExceptionDersomTemaErUgyldig(String tema, String feilmelding) {
		var request = new AvsluttAlleSakerPaaTemaRequest(tema, REFERANSE, AVSLUTTET_DATO, ADMINISTRATIV_ENHET);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(request))
				.withMessage(feilmelding);
	}

	public static Stream<Arguments> skalKasteExceptionDersomTemaErUgyldig() {
		return Stream.of(
				Arguments.of("", "tema kan ikke være null eller tom"),
				Arguments.of(" ", "tema kan ikke være null eller tom"),
				Arguments.of(null, "tema kan ikke være null eller tom"),
				Arguments.of("FA", "tema=FA validerer ikke mot kodeverk. Gyldige verdier for tema er %s".formatted(Arrays.toString(FagomradeCode.values())))
		);
	}

	@ParameterizedTest
	@MethodSource
	void skalKasteExceptionDersomReferanseErForLang(String referanse, String feilmelding) {
		var request = new AvsluttAlleSakerPaaTemaRequest(TEMA, referanse, AVSLUTTET_DATO, ADMINISTRATIV_ENHET);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(request))
				.withMessage(feilmelding);
	}

	public static Stream<Arguments> skalKasteExceptionDersomReferanseErForLang() {
		return Stream.of(
				Arguments.of("", "referanse kan ikke være null eller tom"),
				Arguments.of(" ", "referanse kan ikke være null eller tom"),
				Arguments.of(null, "referanse kan ikke være null eller tom"),
				Arguments.of("REFERANSE_I_JIRA_OVER_MAKSGRENSEN_40_TEGN", "referanse kan ikke være lengre enn 40 tegn. Mottok=REFERANSE_I_JIRA_OVER_MAKSGRENSEN_40_TEGN")
		);
	}

	@Test
	void skalKasteExceptionDersomAvsluttetDatoErIFremtiden() {
		LocalDateTime ugyldigAvsluttetDato = LocalDateTime.now().plusMinutes(1);
		var request = new AvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, ugyldigAvsluttetDato, ADMINISTRATIV_ENHET);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(request))
				.withMessageContaining("avsluttetDato kan ikke være i fremtiden.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"abcd", "123a"})
	void skalKasteExceptionDersomAdministrativEnhetIkkeErEtHeltall(String administrativEnhet) {
		var request = new AvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, AVSLUTTET_DATO, administrativEnhet);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(request))
				.withMessage("administrativEnhet må være et heltall. Mottok=%s".formatted(administrativEnhet));
	}

	@ParameterizedTest
	@ValueSource(strings = {"123", "12345"})
	void skalKasteExceptionDersomAdministrativEnhetErFeilLengde(String administrativEnhet) {
		var request = new AvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, AVSLUTTET_DATO, administrativEnhet);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validerAvsluttAlleSakerPaaTemaRequest(request))
				.withMessage("administrativEnhet må ha en lengde på 4. Mottok=%s".formatted(administrativEnhet));
	}

}