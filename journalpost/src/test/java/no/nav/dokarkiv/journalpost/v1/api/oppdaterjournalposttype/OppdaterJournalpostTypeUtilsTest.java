package no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype.OppdaterJournalpostTypeUtils.determineJournalpostTypeCode;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype.OppdaterJournalpostTypeUtils.determineNewJournalstatusCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OppdaterJournalpostTypeUtilsTest {

	@MethodSource
	@ParameterizedTest
	void shouldReturnExpectedStatusForGivenJournalpostStatusCode(JournalStatusCode currentStatus, JournalStatusCode expectedStatus) {
		assertEquals(expectedStatus, determineNewJournalstatusCode(currentStatus));
	}

	@Test
	void shouldThrowExceptionWhenGivenUnexpectedJournalpostStatusCode(){
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> determineNewJournalstatusCode(FS))
				.withMessageContaining("Ugyldig journalstatus=FS. Gyldige verdier er: [J, M, U, MO, UB]");
	}

	private static Stream<Arguments> shouldReturnExpectedStatusForGivenJournalpostStatusCode() {
		return Stream.of(
				arguments(J, FL),
				arguments(M, D),
				arguments(U, D),
				arguments(UB, D));
	}

	@MethodSource
	@ParameterizedTest
	void shouldReturnExpectedJournalpostTypeCodeForGivenJournalPosttype(JournalpostType journalposttype, JournalpostTypeCode expectedTypeCode) {
		assertEquals(expectedTypeCode, determineJournalpostTypeCode(journalposttype));
	}
	@Test
	void shouldThrowExceptionWhenGivenUnexpectedJournalposttype(){
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> determineJournalpostTypeCode(INNGAAENDE))
				.withMessageContaining("Ugyldig journalposttype=INNGAAENDE. Gyldige verdier er: [UTGAAENDE, NOTAT]");
	}

	private static Stream<Arguments> shouldReturnExpectedJournalpostTypeCodeForGivenJournalPosttype() {
		return Stream.of(
				arguments(UTGAAENDE, JournalpostTypeCode.U),
				arguments(NOTAT, N));
	}
}