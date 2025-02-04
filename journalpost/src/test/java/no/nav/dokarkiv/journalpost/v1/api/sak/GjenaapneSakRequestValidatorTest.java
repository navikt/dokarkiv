package no.nav.dokarkiv.journalpost.v1.api.sak;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.sak.SakRequestValidator.validateGjenaapneSakRequest;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class GjenaapneSakRequestValidatorTest {

	@Test
	void shouldValidateOK() {
		validateGjenaapneSakRequest(createDefaultGjenaapneSakRequest().build());
	}

	@ParameterizedTest
	@MethodSource("generateBrukerAndExpectedResult")
	void validateBruker(Bruker bruker, String expectedExceptionMessage) {
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateGjenaapneSakRequest((createDefaultGjenaapneSakRequest().bruker(bruker).build())))
				.withMessageContaining(expectedExceptionMessage);
	}

	private static Stream<Arguments> generateBrukerAndExpectedResult() {
		return Stream.of(
				Arguments.of(null, "Bruker kan ikke være null."),
				Arguments.of(createBruker(null, FNR), "Bruker.id må være satt"),
				Arguments.of(createBruker("EN_TO_TRE_FIRE", FNR), "Bruker.id må bestå av tall."),
				Arguments.of(createBruker("12345", FNR), "Bruker.id må være 11 siffer for FNR."),
				Arguments.of(createBruker("12345", ORGNR), "Bruker.id må være 9 siffer for ORGNR."),
				Arguments.of(createBruker("12345", AKTOERID), "Bruker.id må være 13 siffer for AKTOERID.")
		);
	}

	@ParameterizedTest
	@MethodSource("generateTemaAndExpectedResult")
	void shouldValidateTema(String tema, String expectedExceptionMessage) {
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateGjenaapneSakRequest(createDefaultGjenaapneSakRequest().tema(tema).build()))
				.withMessageContaining(expectedExceptionMessage);
	}

	private static Stream<Arguments> generateTemaAndExpectedResult() {
		return Stream.of(
				Arguments.of(null, "Mangler påkrevd felt: Tema. Mottok tema="),
				Arguments.of("", "Mangler påkrevd felt: Tema. Mottok tema="),
				Arguments.of("AAAP", "Mottatt tema=AAAP validerer ikke mot kodeverk. Gyldige verdier for tema er"),
				Arguments.of("NEI", "Mottatt tema=NEI validerer ikke mot kodeverk. Gyldige verdier for tema er")
		);
	}

	@ParameterizedTest
	@MethodSource("generateStringsAndExpectedResult")
	void shouldValidateFagsakIdAndFagsaksystem(String fagsakId, String fagsakSystem, String expectedExceptionMessage) {
		var gjenaapneSakRequest = createDefaultGjenaapneSakRequest()
				.fagsakId(fagsakId)
				.fagsaksystem(fagsakSystem).build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateGjenaapneSakRequest(gjenaapneSakRequest))
				.withMessageContaining(expectedExceptionMessage);
	}

	private static Stream<Arguments> generateStringsAndExpectedResult() {
		return Stream.of(
				Arguments.of(null, "AO11", "Mottok ugyldig verdi for feltet fagsakId. Feltet var null/tomt"),
				Arguments.of("", "AO11", "Mottok ugyldig verdi for feltet fagsakId. Feltet var null/tomt"),
				Arguments.of("fagsakId", null, "Mangler påkrevd felt: fagsaksystem. Mottok fagsaksystem=null"),
				Arguments.of("fagsakId", "UGYLDIG_FAGSAKSYSTEM", "Mottatt fagsaksystem=UGYLDIG_FAGSAKSYSTEM validerer ikke mot kodeverk. Gyldige verdier for fagsaksystem er")
		);
	}

	private static Bruker createBruker(String id, BrukerIdType idtype) {
		return Bruker.builder()
				.id(id)
				.idType(idtype)
				.build();
	}

	private GjenaapneSakRequest.GjenaapneSakRequestBuilder createDefaultGjenaapneSakRequest() {
		return GjenaapneSakRequest.builder()
				.tema("BAR")
				.fagsakId("fagsakid123")
				.fagsaksystem("AO11")
				.bruker(Bruker.builder().id("12345678911").idType(FNR).build());
	}

}