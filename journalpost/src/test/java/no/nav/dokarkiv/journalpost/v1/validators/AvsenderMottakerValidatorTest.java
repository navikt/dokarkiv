package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.LOCAL_DATE_TIME;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottaker;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createEnkelJournalpost;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator.validateOppdaterteFelt;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

public class AvsenderMottakerValidatorTest {

	private OppdaterJournalpostRequest oppdaterJournalpostRequest;
	private Journalpost journalpost;


	@ParameterizedTest
	@MethodSource
	void shouldFailWhenAvsenderMottakerIdAndIdTypeIsWrong(String avsenderMottakerId, AvsenderMottakerIdType avsenderMottakerIdType, String resultat) {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottaker(avsenderMottakerId, avsenderMottakerIdType))
				.build();
		Journalpost journalpost = createEnkelJournalpost(M, U);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost))
				.withMessageContaining(
						resultat
				);
	}

	private static Stream<Arguments> shouldFailWhenAvsenderMottakerIdAndIdTypeIsWrong() {
		return Stream.of(
				Arguments.of(AVSENDER_ID_PERSON, null, "Oppdatering av avsenderMottaker.id krever at feltet avsenderMottaker.idType er satt. Mottatt id=12345***** idType=null"),
				Arguments.of(null, AvsenderMottakerIdType.FNR, "Oppdatering av avsenderMottaker.idType krever at feltet avsenderMottaker.id er satt. Mottatt id=null idType=FNR"),
				Arguments.of("", AvsenderMottakerIdType.FNR, "Oppdatering av avsenderMottaker.idType krever at feltet avsenderMottaker.id er satt."),
				Arguments.of("  ", AvsenderMottakerIdType.FNR, "Oppdatering av avsenderMottaker.idType krever at feltet avsenderMottaker.id er satt."),
				Arguments.of("1234567890", AvsenderMottakerIdType.FNR, "avsenderMottaker.id må være 11 siffer dersom avsenderMottaker.idType=FNR."),
				Arguments.of("1234567890a", AvsenderMottakerIdType.FNR, "avsenderMottaker.id må være 11 siffer dersom avsenderMottaker.idType=FNR."),
				Arguments.of("1234567891012", AvsenderMottakerIdType.FNR, "avsenderMottaker.id må være 11 siffer dersom avsenderMottaker.idType=FNR."),
				Arguments.of("12345678", AvsenderMottakerIdType.ORGNR, "avsenderMottaker.id må være 9 siffer dersom avsenderMottaker.idType=ORGNR."),
				Arguments.of("12345678a", AvsenderMottakerIdType.ORGNR, "avsenderMottaker.id må være 9 siffer dersom avsenderMottaker.idType=ORGNR."),
				Arguments.of("12345678910", AvsenderMottakerIdType.ORGNR, "avsenderMottaker.id må være 9 siffer dersom avsenderMottaker.idType=ORGNR."),
				Arguments.of("123456", AvsenderMottakerIdType.HPRNR, "avsenderMottaker.id må være 7-9 siffer dersom avsenderMottaker.idType=HPRNR."),
				Arguments.of("123456a", AvsenderMottakerIdType.HPRNR, "avsenderMottaker.id må være 7-9 siffer dersom avsenderMottaker.idType=HPRNR."),
				Arguments.of("1234567891", AvsenderMottakerIdType.HPRNR, "avsenderMottaker.id må være 7-9 siffer dersom avsenderMottaker.idType=HPRNR.")
		);
	}

	// Det skal ikke være lov til å oppdatere avsenderMottaker (id, navn) for utgående, ferdigstilte journalposter.
	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"FL", "FS", "E"})
	void shouldFailWhenAvsenderMottakerNavnOrIdIsSetForTypeU(JournalStatusCode input) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.build();
		journalpost = createEnkelJournalpost(input, U);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost))
				.withMessageContaining(
						format("avsenderMottaker.id kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=U", input),
						format("avsendeMottaker.navn kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=U", input)
				);
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"FL", "FS", "E", "D", "A"})
	public void shouldFailWhenAvsenderMottakerNavnOrIdIsSetForTypeN(JournalStatusCode input) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.build();
		journalpost = createEnkelJournalpost(input, N);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost))
				.withMessageContaining(
						format("avsenderMottaker.id kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=N", input),
						format("avsendeMottaker.navn kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=N", input)
				);
	}

	@Test
	public void shouldValidateWhenAvsenderMottakerNavnOrIdIsSetForStatusJ() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.build();
		journalpost = createEnkelJournalpost(J, U);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@ParameterizedTest
	@MethodSource
	public void shouldValidateAvsenderMottakerWhenBothIdAndTypeIsSetOrNotSet(String id, AvsenderMottakerIdType idType) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(id)
						.idType(idType)
						.build())
				.build();
		journalpost = createEnkelJournalpost(J, I);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	private static Stream<Arguments> shouldValidateAvsenderMottakerWhenBothIdAndTypeIsSetOrNotSet() {
		return Stream.of(
				Arguments.of(AVSENDER_ID_PERSON, AvsenderMottakerIdType.FNR),
				Arguments.of(AVSENDER_ID_ORGANISASJON, AvsenderMottakerIdType.ORGNR),
				Arguments.of(" ", AvsenderMottakerIdType.FNR),
				Arguments.of("", null),
				Arguments.of(null, null)
		);
	}

	@Test
	public void shoudThrowExceptionWhenUpdatingAvsenderMottakerOnOldJournapost() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(AVSENDER_ID_PERSON)
						.idType(AvsenderMottakerIdType.FNR)
						.navn(AVSENDER_NAVN)
						.build())
				.tittel(DOKUMENT_TITTEL1)
				.build();
		journalpost = createEnkelJournalpost(J, I);
		journalpost.setJournalDato(LOCAL_DATE_TIME);

		String journalDatoAsString = journalpost.getJournalDato().toString();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost))
				.withMessageContainingAll(
						format("avsenderMottaker.id kan ikke oppdateres da journalposten er journalført for over 1 år siden. journalDato=%s", journalDatoAsString),
						format("avsenderMottaker.navn kan ikke oppdateres da journalposten er journalført for over 1 år siden. journalDato=%s", journalDatoAsString),
						format("tittel kan ikke oppdateres da journalposten er journalført for over 1 år siden. journalDato=%s", journalDatoAsString)
				);
	}
}
