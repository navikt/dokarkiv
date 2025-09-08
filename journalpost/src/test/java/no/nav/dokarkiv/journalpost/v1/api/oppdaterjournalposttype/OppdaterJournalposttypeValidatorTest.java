package no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype.OppdaterJournalposttypeValidator.validateJournalpostKanEndres;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype.OppdaterJournalposttypeValidator.validateOppdaterJournalpostTypeRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class OppdaterJournalposttypeValidatorTest {
	@ParameterizedTest
	@MethodSource("provideInvalidJournalpostsForUpdating")
	void shouldThrowExceptionWhenNotJournalpostKanEndres(Journalpost journalpost, String expectedMessage) {
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateJournalpostKanEndres(journalpost))
				.withMessageContaining(expectedMessage);
	}

	private static Stream<Arguments> provideInvalidJournalpostsForUpdating() {
		return Stream.of(
				arguments(createMinimalJournalpost(JournalpostTypeCode.N, JournalStatusCode.M), "Journalpost med journalpostId=123 har journalposttype=N og kan derfor ikke endres. Kun journalposter med journalposttype=I kan endres."),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.FL), "Journalpost med journalpostId=123 har journalstatus=FL og kan derfor ikke endres. Kun journalposter med journalstatus=[J, M, U, MO, UB] kan endres."),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.A), "Journalpost med journalpostId=123 har journalstatus=A og kan derfor ikke endres. Kun journalposter med journalstatus=[J, M, U, MO, UB] kan endres."),
				arguments(createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.D), "Journalpost med journalpostId=123 har journalstatus=D og kan derfor ikke endres. Kun journalposter med journalstatus=[J, M, U, MO, UB] kan endres.")
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
	void shouldValidateJournalpostKanEndres(String journalstatusCode) {
		Journalpost jp = createMinimalJournalpost(JournalpostTypeCode.I, JournalStatusCode.valueOf(journalstatusCode));
		assertDoesNotThrow(() -> validateJournalpostKanEndres(jp));
	}

	@ParameterizedTest
	@CsvSource({"12", "abcd", "12345", "123a"})
	void shouldThrowExeptionWhenValidatingJournalfoerendeEnhet(String journalfoerendeEnhet) {
		OppdaterJournalposttypeRequest request = createMinimalOppdaterJournalpostTypeRequest(journalfoerendeEnhet, UTGAAENDE);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateOppdaterJournalpostTypeRequest(request))
				.withMessageContaining("Ugyldig journalfoerendeEnhet, må være 4 siffer. Mottok: " + journalfoerendeEnhet);
	}

	@NullSource
	@EmptySource
	@ParameterizedTest
	@CsvSource({"1234"})
	void shouldNotThrowExceptionWhenValidatingJournalfoerendeEnhet(String journalfoerendeEnhet) {
		OppdaterJournalposttypeRequest request = createMinimalOppdaterJournalpostTypeRequest(journalfoerendeEnhet, UTGAAENDE);

		assertDoesNotThrow(() -> validateOppdaterJournalpostTypeRequest(request));
	}

	@NullSource
	@ParameterizedTest
	@CsvSource({"INNGAAENDE"})
	void shouldThrowExceptionWhenGivenBadTypeEndresTil(JournalpostType typeEndresTil) {
		OppdaterJournalposttypeRequest request = createMinimalOppdaterJournalpostTypeRequest("1234", typeEndresTil);


		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateOppdaterJournalpostTypeRequest(request))
				.withMessageContaining("Ugyldig typeEndresTil, kan kun endres til UTGAAENDE eller NOTAT. Mottok: " + typeEndresTil);
	}

	@ParameterizedTest
	@CsvSource({"UTGAAENDE", "NOTAT"})
	void shouldValidateTypeEndresTil(JournalpostType typeEndresTil) {
		OppdaterJournalposttypeRequest request = createMinimalOppdaterJournalpostTypeRequest("1234", typeEndresTil);

		assertDoesNotThrow(() -> validateOppdaterJournalpostTypeRequest(request));
	}

	private static OppdaterJournalposttypeRequest createMinimalOppdaterJournalpostTypeRequest(String journalfoerendeEnhet, JournalpostType typeEndresTil) {
		return new OppdaterJournalposttypeRequest(typeEndresTil, journalfoerendeEnhet);
	}

	private static Journalpost createMinimalJournalpost(JournalpostTypeCode journalpostTypeCode, JournalStatusCode journalStatusCode) {
		return Journalpost.builder()
				.journalpostId(123L)
				.journalposttype(journalpostTypeCode)
				.journalstatus(journalStatusCode)
				.build();
	}
}
