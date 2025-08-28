package no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalpostType;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalpostType.OppdaterJournalpostTypeUtils.determineJournalpostTypeCode;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalpostType.OppdaterJournalpostTypeUtils.determineNewJournalstatusCode;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalpostType.OppdaterJournalpostTypeUtils.validateJournalpostKanEndres;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalpostType.OppdaterJournalpostTypeUtils.validateOppdaterJournalpostTypeRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OppdaterJournalpostTypeUtilsTest {

	@ParameterizedTest
	@MethodSource("provideJournalStatusCodes")
	void shouldReturnExpectedStatusForGivenJournalpostStatusCode(JournalStatusCode currentStatus, JournalStatusCode expectedStatus) {
		assertEquals(expectedStatus, determineNewJournalstatusCode(currentStatus));
	}

	private static Stream<Arguments> provideJournalStatusCodes() {
		return Stream.of(
				arguments(J, FL),
				arguments(M, D),
				arguments(U, D),
				arguments(UB, D));
	}

	@ParameterizedTest
	@MethodSource("provideJournalpostTypeCases")
	void shouldReturnExpectedJournalpostTypeCodeForGivenJournalPostType(String journalpostType, JournalpostTypeCode expectedTypeCode) {
		assertEquals(expectedTypeCode, determineJournalpostTypeCode(journalpostType));
	}

	private static Stream<Arguments> provideJournalpostTypeCases() {
		return Stream.of(
				arguments("UTGAAENDE", JournalpostTypeCode.U),
				arguments("NOTAT", N)
		);
	}

	@ParameterizedTest
	@MethodSource("provideInvalidJournalpostsForUpdating")
	void shouldThrowExceptionWhenNotJournalpostKanEndres(Journalpost journalpost, String expectedMessage) {
		InputValideringFeiletException exception = assertThrows(InputValideringFeiletException.class,
				() -> validateJournalpostKanEndres(journalpost));
		assertThat(exception.getMessage()).contains(expectedMessage);
	}

	private static Stream<Arguments> provideInvalidJournalpostsForUpdating() {
		return Stream.of(
				arguments(createMinimalJournalpost(JournalpostTypeCode.N, JournalStatusCode.M), "Journalpost med journalpostId=123 har journalposttype=N og kan derfor ikke endres. Kun journalposter med journalposttype=I kan endres."),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.FL), "Journalpost med journalpostId=123 har journalstatus=FL og kan derfor ikke endres. Kun journalposter med journalstatus=[M, MO, U, UB, J] kan endres."),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.A), "Journalpost med journalpostId=123 har journalstatus=A og kan derfor ikke endres. Kun journalposter med journalstatus=[M, MO, U, UB, J] kan endres."),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.D), "Journalpost med journalpostId=123 har journalstatus=D og kan derfor ikke endres. Kun journalposter med journalstatus=[M, MO, U, UB, J] kan endres.")
		);
	}

	@ParameterizedTest
	@MethodSource("provideValidJournalpostsForUpdating")
	void shouldValidateJournalpostKanEndres(Journalpost journalpost) {
		assertDoesNotThrow(() -> validateJournalpostKanEndres(journalpost));
	}

	private static Stream<Arguments> provideValidJournalpostsForUpdating() {
		return Stream.of(
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.MO)),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.U)),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.UB)),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.J))
		);
	}

	@ParameterizedTest
	@CsvSource({"M", "MO", "U", "UB", "J"})
	void shouldValidateJournalpostKanEndres() {
		Journalpost jp = createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.M);
		assertDoesNotThrow(() -> validateJournalpostKanEndres(jp));
	}

	@ParameterizedTest
	@MethodSource("provideUgyldigeJournalfoerendeEnheter")
	void shouldThrowExeptionWhenValidatingJournalfoerendeEnhet(String journalfoerendeEnhet, String expectedMessage) {
		OppdaterJournalpostTypeRequest request = createMinimalOppdaterJournalpostTypeRequest(journalfoerendeEnhet, "UTGAAENDE");

		InputValideringFeiletException exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterJournalpostTypeRequest(request));
		assertThat(exception.getMessage()).contains(expectedMessage);

	}

	private static Stream<Arguments> provideUgyldigeJournalfoerendeEnheter() {
		return Stream.of(
				arguments("12", "Ugyldig journalfoerendeEnhet, må være 4 siffer. Mottok: 12"),
				arguments("abcd", "Ugyldig journalfoerendeEnhet, må være 4 siffer. Mottok: abcd")
		);
	}

	@NullSource
	@EmptySource
	@ParameterizedTest
	@CsvSource({"1234"})
	void shouldNotThrowExceptionWhenValidatingJournalfoerendeEnhet(String journalfoerendeEnhet) {
		OppdaterJournalpostTypeRequest request = createMinimalOppdaterJournalpostTypeRequest(journalfoerendeEnhet, "UTGAAENDE");

		assertDoesNotThrow(() -> validateOppdaterJournalpostTypeRequest(request));
	}

	@ParameterizedTest
	@MethodSource("provideUgyldigeEndresTil")
	void shouldThrowExceptionWhenValidatingTypeEndresTil(String typeEndresTil, String expectedMessage) {
		OppdaterJournalpostTypeRequest request = createMinimalOppdaterJournalpostTypeRequest("1234", typeEndresTil);
			InputValideringFeiletException exception = assertThrows(InputValideringFeiletException.class,
					() -> validateOppdaterJournalpostTypeRequest(request));
			assertThat(exception.getMessage()).contains(expectedMessage);
	}

	private static Stream<Arguments> provideUgyldigeEndresTil() {
		return Stream.of(
				arguments("INVALID", "Ugyldig typeEndresTil, kan kun endres til UTGAAENDE eller NOTAT. Mottok: INVALID"),
				arguments("", "Ugyldig typeEndresTil, kan kun endres til UTGAAENDE eller NOTAT. Mottok: "),
				arguments(null, "Ugyldig typeEndresTil, kan kun endres til UTGAAENDE eller NOTAT. Mottok: null")
		);
	}

	@ParameterizedTest
	@CsvSource({"UTGAAENDE", "NOTAT"})
	void shouldNotThrowExceptionWhenValidatingTypeEndresTil(String typeEndresTil) {
		OppdaterJournalpostTypeRequest request = createMinimalOppdaterJournalpostTypeRequest("1234", typeEndresTil);

		assertDoesNotThrow(() -> validateOppdaterJournalpostTypeRequest(request));
	}

	private static OppdaterJournalpostTypeRequest createMinimalOppdaterJournalpostTypeRequest(String journalfoerendeEnhet, String typeEndresTil) {
		return OppdaterJournalpostTypeRequest.builder()
				.journalfoerendeEnhet(journalfoerendeEnhet)
				.typeEndresTil(typeEndresTil).build();
	}

	private static Journalpost createMinimalJournalpost(JournalpostTypeCode journalpostTypeCode, JournalStatusCode journalStatusCode) {
		return Journalpost.builder()
				.journalpostId(123L)
				.journalposttype(journalpostTypeCode)
				.journalstatus(journalStatusCode)
				.build();
	}
}