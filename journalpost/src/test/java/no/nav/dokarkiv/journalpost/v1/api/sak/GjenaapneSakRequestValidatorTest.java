package no.nav.dokarkiv.journalpost.v1.api.sak;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
	@MethodSource("validateBruker")
	void validateBruker(Bruker bruker, String expectedExceptionMessage) {
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateGjenaapneSakRequest((createDefaultGjenaapneSakRequest().bruker(bruker).build())))
				.withMessageContaining(expectedExceptionMessage);
	}

	private static Stream<Arguments> validateBruker() {
		return Stream.of(
				Arguments.of(null, "bruker kan ikke være null."),
				Arguments.of(createBruker(null, FNR), "bruker.id må være satt"),
				Arguments.of(createBruker("EN_TO_TRE_FIRE", FNR), "bruker.id må bestå av tall."),
				Arguments.of(createBruker("12345", null), "bruker.idType kan ikke være null."),
				Arguments.of(createBruker("12345", FNR), "bruker.id må være 11 siffer dersom bruker.idType=FNR."),
				Arguments.of(createBruker("12345", ORGNR), "bruker.id må være 9 siffer dersom bruker.idType=ORGNR."),
				Arguments.of(createBruker("12345", AKTOERID), "bruker.id må være 13 siffer dersom bruker.idType=AKTOERID.")
		);
	}

	@ParameterizedTest
	@MethodSource("shouldValidateTema")
	void shouldValidateTema(String tema, String expectedExceptionMessage) {
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateGjenaapneSakRequest(createDefaultGjenaapneSakRequest().tema(tema).build()))
				.withMessageContaining(expectedExceptionMessage);
	}

	private static Stream<Arguments> shouldValidateTema() {
		return Stream.of(
				Arguments.of("", "Mangler påkrevd felt: tema. Mottok tema="),
				Arguments.of(null, "Mangler påkrevd felt: tema. Mottok tema=null"),
				Arguments.of("NEI", "Mottatt tema=NEI validerer ikke mot kodeverk. Gyldige verdier for tema er"),
				Arguments.of("AAAP", "Mottatt tema=AAAP validerer ikke mot kodeverk. Gyldige verdier for tema er")
		);
	}

	@ParameterizedTest
	@MethodSource("shouldValidateFagsakIdAndFagsaksystem")
	void shouldValidateFagsakIdAndFagsaksystem(String fagsakId, String fagsakSystem, String expectedExceptionMessage) {
		var gjenaapneSakRequest = createDefaultGjenaapneSakRequest()
				.fagsakId(fagsakId)
				.fagsaksystem(fagsakSystem).build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateGjenaapneSakRequest(gjenaapneSakRequest))
				.withMessageContaining(expectedExceptionMessage);
	}

	private static Stream<Arguments> shouldValidateFagsakIdAndFagsaksystem() {
		return Stream.of(
				Arguments.of("", "AO11", "Mottok ugyldig verdi for feltet fagsakId. Feltet var null/tomt"),
				Arguments.of(null, "AO11", "Mottok ugyldig verdi for feltet fagsakId. Feltet var null/tomt"),
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
				.fagsaksystem("AO11")
				.fagsakId("fagsakid123")
				.bruker(Bruker.builder().id("12345678911").idType(FNR).build());
	}

}