package no.nav.dokarkiv.journalpost.v1.api.avsluttSak;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.avsluttSak.AvsluttSakRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.avsluttSak.AvsluttSakValidator.validateAvsluttSakRequest;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AvsluttSakValidatorTest {


	@Test
	void shouldValidateOK() {
		validateAvsluttSakRequest(createDefaultAvsluttSakRequestBuilder().build());
	}

	@ParameterizedTest
	@MethodSource("generateBrukerAndExpectedResult")
	void validateBruker(Bruker bruker, String expectedExceptionMessage) {
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateAvsluttSakRequest((createDefaultAvsluttSakRequestBuilder().bruker(bruker).build())))
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
				.isThrownBy(() -> validateAvsluttSakRequest(createDefaultAvsluttSakRequestBuilder().tema(tema).build()))
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
	void shouldValidateRequiredStrings(String fagsakId, String fagsakSystem, String administrativEnhet, String sakAnsvarlig, String expectedExceptionMessage) {
		var avsluttSakRequest = createDefaultAvsluttSakRequestBuilder()
				.fagsakId(fagsakId)
				.fagsaksystem(fagsakSystem)
				.administrativEnhet(administrativEnhet)
				.sakAnsvarlig(sakAnsvarlig).build();
		Exception thrown = assertThrows(InputValideringFeiletException.class, () -> validateAvsluttSakRequest(avsluttSakRequest));
		assertThat(thrown.getMessage()).contains(expectedExceptionMessage);
	}

	private static Stream<Arguments> generateStringsAndExpectedResult() {
		return Stream.of(
				Arguments.of(null, "fagsaksystem", "AdministrativEnhet", null, "Mottok ugyldig verdi for feltet fagsakId. Feltet var null/tomt"),
				Arguments.of("", "fagsaksystem", "AdministrativEnhet", null, "Mottok ugyldig verdi for feltet fagsakId. Feltet var null/tomt"),
				Arguments.of("fagsakId", null, "AdministrativEnhet", null, "Mottok ugyldig verdi for feltet fagsaksystem. Feltet var null/tomt"),
				Arguments.of("fagsakId", "fagsaksystem", null, null, "Mottok ugyldig verdi for feltet administrativEnhet. Feltet var null/tomt"),
				Arguments.of("fagsakId", "fagsaksystem", null, null, "Mottok ugyldig verdi for feltet administrativEnhet. Feltet var null/tomt")
		);
	}

	@ParameterizedTest
	@MethodSource()
	void shouldValidateDate(LocalDateTime opprettetTidspunkt, LocalDateTime avsluttetDato, String expectedExceptionMessage) {
		var avsluttSakRequest = createDefaultAvsluttSakRequestBuilder()
				.opprettetDato(opprettetTidspunkt)
				.avsluttetDato(avsluttetDato).build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() ->  validateAvsluttSakRequest(avsluttSakRequest))
				.withMessageContaining(expectedExceptionMessage);
	}

	private static Stream<Arguments> shouldValidateDate() {
		return Stream.of(
				Arguments.of(LocalDateTime.now().plusSeconds(25), null, "Validering av opprettetDato feilet. Dato kan ikke være frem i tid."),
				Arguments.of(null, null, "Validering av opprettetDato feilet. Dato kan ikke være null"),
				Arguments.of(LocalDateTime.now(), LocalDateTime.now().plusSeconds(25), "Validering av avsluttetDato feilet. Dato kan ikke være frem i tid.")
		);
	}

	private static Bruker createBruker(String id, BrukerIdType idtype) {
		return Bruker.builder().id(id).idType(idtype).build();
	}

	private AvsluttSakRequest.AvsluttSakRequestBuilder createDefaultAvsluttSakRequestBuilder() {
		return AvsluttSakRequest.builder()
				.tema("BAR")
				.fagsakId("fagsakid123")
				.fagsaksystem("A01")
				.bruker(Bruker.builder().id("12345678911").idType(FNR).build())
				.opprettetDato(LocalDateTime.MIN)
				.administrativEnhet("AdministrativEnhet");
	}
}
