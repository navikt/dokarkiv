package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Stream;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem.GSAK;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.AO01;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.ARKIVSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.ARKIVSAKSNUMMER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTINFO_ID1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.LOCAL_DATE_TIME;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBrukerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createEnkelJournalpost;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createSak;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.SKJULT_TITTEL;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator.LOVLIGE_INNSYNSKODER;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator.validateOppdaterteFelt;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;

public class OppdaterFerdigstillJournalpostValidatorTest {

	private OppdaterJournalpostRequest oppdaterJournalpostRequest;
	private Journalpost journalpost;

	@Test
	public void happyPath() {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();
		journalpost = createEnkelJournalpost(M, I);
		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@Test
	public void happyPathFagsak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.datoDokument(LocalDateTime.now().minusDays(2))
				.build();
		journalpost = createEnkelJournalpost(M, I);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@Test
	public void happyPathGenerellSak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@Test
	public void happyPathArkivsak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(GSAK)
						.build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@Test
	public void happyPathTemaPEN() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@Test
	public void happyPathTemaUFO() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForFagsak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		var exception = assertThrows(InputValideringFeiletException.class, () -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForGenerellSak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).fagsakId(FAGSAK_ID).build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForArkivsak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(GSAK)
						.fagsakId(FAGSAK_ID)
						.build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Sak.fagsakId");
	}


	// Det skal ikke være lov til å oppdatere avsenderMottaker (id, navn) for utgående, ferdigstilte journalposter. .
	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"FL", "FS", "E"})
	void shouldFailWhenAvsenderMottakerNavnOrIdIsSetForTypeU(JournalStatusCode input) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.build();
		journalpost = createEnkelJournalpost(input, U);

		var exception = assertThrows(InputValideringFeiletException.class, () ->
				validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains(
				format("AvsendeMottakerId kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=U", input),
				format("AvsendeMottakerNavn kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=U", input));
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"FL", "FS", "E", "D", "A"})
	public void shouldFailWhenAvsenderMottakerNavnOrIdIsSetForTypeN(JournalStatusCode input) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.build();
		journalpost = createEnkelJournalpost(input, N);

		var exception = assertThrows(InputValideringFeiletException.class, () ->
				validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains(
				format("AvsendeMottakerId kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=N", input),
				format("AvsendeMottakerNavn kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=N", input));

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
	@EnumSource(value = JournalStatusCode.class, names = {"FL", "FS", "E"})
	void shouldFailIfTittelIsSetForStatusFL(JournalStatusCode input) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tittel("tittel")
				.build();
		journalpost = createEnkelJournalpost(input, U);

		var exception = assertThrows(InputValideringFeiletException.class, () ->
				validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains(
				format("Tittel kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=U", input));
	}

	@ParameterizedTest
	@ValueSource(strings = {"FL", "FS", "E"})
	void shouldFailIfTittelIsSetForJournalPostTypeN(String input) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tittel("tittel")
				.build();
		journalpost = createEnkelJournalpost(JournalStatusCode.valueOf(input), N);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains(String.format("Tittel kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=N", input));
	}

	@Test
	public void shouldFailWhenJournalpostTittelFemStjerner() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tittel(SKJULT_TITTEL)
				.build();
		journalpost = createEnkelJournalpost(J, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Tittel kan ikke oppdateres til *****");
	}

	@Test
	public void shouldFailWhenDokumentTittelFemStjerner() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.dokumenter(Collections.singletonList(DokumentInfo.builder()
						.dokumentInfoId(DOKUMENTINFO_ID1)
								.tittel(SKJULT_TITTEL)
						.build()))
				.build();
		journalpost = createEnkelJournalpost(J, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Dokumenter.tittel kan ikke oppdateres til *****");
	}

	// Det skal alltid være lov til å endre brevkode. Se commit.
	@Test
	public void shouldUpdateBrevkode() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.dokumenter(Collections.singletonList(
						DokumentInfo.builder()
								.brevkode("oppdatert")
								.dokumentInfoId(DOKUMENTINFO_ID1)
								.build())).build();
		journalpost = createEnkelJournalpost(FS, U);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@Test
	public void shouldFailIfBrukerSetForStatusJ() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.bruker(createBrukerPerson())
				.build();
		journalpost = createEnkelJournalpost(J, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker kan ikke oppdateres for journalpost med journalpoststatus=J og journalposttype=I");
	}

	@Test
	public void shouldFailIfSakSetForStatusJ() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(createSak())
				.build();
		journalpost = createEnkelJournalpost(J, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Sak kan ikke oppdateres for journalpost med journalpoststatus=J og journalposttype=I");
	}

	@Test
	public void shouldThrowExceptionWhenSakArkivsaksnummerNotNumericAndJournalstatusM() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksnummer("quack123")
						.arkivsaksystem(GSAK)
						.build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksnummer må være et heltall, og saken må være opprettet i GSAK/PSAK");
	}

	@Test
	public void shouldFailIfJournalFoerendeEnhetSetForStatusJ() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().journalfoerendeEnhet(JOURNALFOERENDE_ENHET).build();
		journalpost = createEnkelJournalpost(J, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("JournalfoerendeEnhet kan ikke oppdateres for journalpost med journalpoststatus=J og journalposttype=I");
	}

	@Test
	public void shouldFailIfTemaSetForStatusJ() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().tema(TEMA_FOR).build();
		journalpost = createEnkelJournalpost(J, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Tema kan ikke oppdateres for journalpost med journalpoststatus=J og journalposttype=I");
	}

	@Test
	public void shouldFailIfBrukerSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.bruker(createBrukerPerson())
				.build();
		journalpost = createEnkelJournalpost(FS, U);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker kan ikke oppdateres for journalpost med journalpoststatus=FS og journalposttype=U");
	}

	@Test
	public void shouldFailIfSakSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(createSak())
				.build();
		journalpost = createEnkelJournalpost(FS, U);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Sak kan ikke oppdateres for journalpost med journalpoststatus=FS og journalposttype=U");
	}

	@Test
	public void shouldFailIfJournalFoerendeEnhetSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().journalfoerendeEnhet(JOURNALFOERENDE_ENHET).build();
		journalpost = createEnkelJournalpost(FS, U);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("JournalfoerendeEnhet kan ikke oppdateres for journalpost med journalpoststatus=FS og journalposttype=U");
	}

	@Test
	public void shouldFailIfTemaSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().tema(TEMA_FOR).build();
		journalpost = createEnkelJournalpost(FS, U);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Tema kan ikke oppdateres for journalpost med journalpoststatus=FS og journalposttype=U");
	}

	@Test
	public void shouldFailIfDatoReturSetForStatusFSAndNotat() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().datoRetur(Date.valueOf(LOCAL_DATE_TIME.toLocalDate())).build();
		journalpost = createEnkelJournalpost(FS, N);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("DatoRetur kan ikke oppdateres for journalpost med journalpoststatus=FS og journalposttype=N");
	}

	@Test
	public void shouldFailIfDatoDokumenIsFremtid() {

		var datoDokument = LocalDateTime.now().plusDays(2);

		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.datoDokument(datoDokument)
				.build();
		journalpost = createEnkelJournalpost(FS, N);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));

		assertThat(exception.getMessage()).contains(
				format("%s er ugyldig verdi for datoDokument. Feltet kan ikke settes frem i tid. Nåtid er ", datoDokument));
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, mode = INCLUDE, names = {
			"BRUK_STANDARDREGLER", "VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT",
			"SKJULES_FEILSENDT", /*"SKJULES_BRUKERS_SIKKERHET",*/ "SKJULES_BRUKERS_ONSKE"
	})
	@NullSource
	public void shouldNotFailIfOverstyrInnsynsreglerIsValid(InnsynCode overstyrInnsynsregler) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.overstyrInnsynsregler(overstyrInnsynsregler != null ? overstyrInnsynsregler.name() : null)
				.build();
		journalpost = createEnkelJournalpost(M, I);

		assertDoesNotThrow(() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, mode = EXCLUDE, names = {
			"BRUK_STANDARDREGLER", "VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT",
			"SKJULES_FEILSENDT", /*"SKJULES_BRUKERS_SIKKERHET",*/ "SKJULES_BRUKERS_ONSKE"
	})
	public void shouldFailIfOverstyrInnsynsreglerIsInvalid(InnsynCode overstyrInnsynsregler) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.overstyrInnsynsregler(overstyrInnsynsregler.name())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		var exception = assertThrows(InputValideringFeiletException.class, () -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));

		assertThat(exception.getMessage()).contains(format("OverstyrInnsynsregler må være en av følgende verdier: null eller %s. Mottatt: %s", LOVLIGE_INNSYNSKODER, overstyrInnsynsregler));
	}

	@Test
	public void shouldTNotValidateBrukerWhenSaksTypeIsArkivsak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(ARKIVSAK)
						.arkivsaksnummer("11111")
						.arkivsaksystem(GSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder().id("test999999").idType(ORGNR).build())
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("9999999999")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();
		journalpost = createEnkelJournalpost(D, I);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);
	}

	@Test
	public void shouldFailIfBrukerIdIsNull() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.bruker(Bruker.builder().idType(FNR).build())
				.tema("DAG")
				.sak(Sak.builder()
						.fagsakId("10695768")
						.sakstype(FAGSAK)
						.fagsaksystem(AO01)
						.build())
				.build();
		journalpost = createEnkelJournalpost(D, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker.id og Bruker.idType må være satt dersom sakstype=FAKSAK. Mottatt id=null idType=FNR");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRMoreThan9Digits() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder().id("9999999999").idType(ORGNR).build())
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("9999999999")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();
		journalpost = createEnkelJournalpost(D, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker.id må være 9 siffer for Bruker.idType=ORGNR. Mottatt id=99999*****");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNotNumeric() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder()
						.idType(FNR)
						.id("abc11111111")
						.build())
				.build();
		journalpost = createEnkelJournalpost(D, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker.id kan kun bestå av tall. Mottatt id=abc11111111");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForFnr() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder()
						.idType(FNR)
						.id("1122334455")
						.build())
				.build();
		journalpost = createEnkelJournalpost(D, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker.id må være 11 siffer for Bruker.idType=FNR. Mottatt id=11223***** har lengde=10");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForOrgnr() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder()
						.idType(ORGNR)
						.id("1122334455")
						.build())
				.build();
		journalpost = createEnkelJournalpost(D, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker.id må være 9 siffer for Bruker.idType=ORGNR. Mottatt id=11223***** har lengde=10");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForAktoerid() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder()
						.idType(AKTOERID)
						.id("1122334455")
						.build())
				.build();
		journalpost = createEnkelJournalpost(D, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker.id må være 11 siffer for Bruker.idType=AKTOERID. Mottatt id=11223***** har lengde=10");
	}

	@Test
	public void shouldFailIfBrukerIsNull() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.bruker(Bruker.builder().build())
				.tema("DAG")
				.sak(Sak.builder()
						.fagsakId("10695768")
						.sakstype(FAGSAK)
						.fagsaksystem(AO01)
						.build())
				.build();
		journalpost = createEnkelJournalpost(D, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Bruker.id og Bruker.idType må være satt dersom sakstype=FAKSAK. Mottatt id=null idType=null");
	}

	@ParameterizedTest
	@ValueSource(strings = {"bb3333", "abc123", "ab12345", "ab123", "1234ab", "1ab1234", "bab1234"})
	public void shouldThrowExceptionWhenInvalidBehandlingstema(String behandlingstema) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.behandlingstema(behandlingstema)
				.bruker(Bruker.builder().idType(FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		InputValideringFeiletException e = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertEquals(String.format("Behandlingstema må være på formatet ´ab + 4 siffer´. Mottatt behandlingstema=%s", behandlingstema),
				e.getMessage());
	}

	@ParameterizedTest
	@MethodSource
	public void shouldThrowExceptionOnAvsenderMottakerUpdateMismatch(String id, AvsenderMottakerIdType idType) {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(id)
						.idType(idType)
						.build())
				.build();
		journalpost = createEnkelJournalpost(J, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).containsAnyOf(
				"Oppdatering av avsenderMottaker.id for journalpost med journalposttype=INNGAAENDE krever at feltet avsenderMottaker.idType er satt.",
				"Oppdatering av avsenderMottaker.idType for journalpost med journalposttype=INNGAAENDE krever at feltet avsenderMottaker.id er satt.");
	}

	private static Stream<Arguments> shouldThrowExceptionOnAvsenderMottakerUpdateMismatch() {
		return Stream.of(
				Arguments.of(AVSENDER_ID_PERSON, null),
				Arguments.of(null, AvsenderMottakerIdType.FNR),
				Arguments.of("", AvsenderMottakerIdType.FNR)
		);
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
				Arguments.of(AVSENDER_ID_ORGANISASJON, AvsenderMottakerIdType.FNR),
				Arguments.of(" ", AvsenderMottakerIdType.FNR),
				Arguments.of("", null),
				Arguments.of(null, null)
		);
	}

	@Test
	void shouldThrowExceptionWhenFagsakAndFagsystemPP01AndFagsakIdNotNumeric() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(PP01).build())
				.build();
		journalpost = createEnkelJournalpost(M, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains("Sak.fagsakId må være et heltall for saker opprettet i PSAK");
	}

	@Test
	void shouldThrowExceptionWithMultipleValidationErrorMessages() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema("test")
				.bruker(Bruker.builder()
						.idType(FNR)
						.id(BRUKER_ID_PERSON)
						.build())
				.sak(Sak.builder()
						.sakstype(FAGSAK).
						fagsakId(FAGSAK_ID).
						fagsaksystem(PP01)
						.build())
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.avsenderMottaker(createAvsenderMottakerPerson())
				.build();
		journalpost = createEnkelJournalpost(J, I);

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains(
				"Bruker kan ikke oppdateres for journalpost med journalpoststatus=J og journalposttype=I",
				"Sak kan ikke oppdateres for journalpost med journalpoststatus=J og journalposttype=I",
				"Tema kan ikke oppdateres for journalpost med journalpoststatus=J og journalposttype=I",
				"JournalfoerendeEnhet kan ikke oppdateres for journalpost med journalpoststatus=J og journalposttype=I",
				"Oppdatering av avsenderMottaker.id for journalpost med journalposttype=INNGAAENDE krever at feltet avsenderMottaker.idType er satt.");
	}

	@Test
	public void shoudThrowExceptionWhenUpdatingAvsenderOnOldJournapost() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(AVSENDER_ID_PERSON)
						.idType(AvsenderMottakerIdType.FNR)
						.navn(AVSENDER_NAVN)
						.build())
				.tittel(DOKUMENT_TITTEL1)
				.build();
		journalpost = createEnkelJournalpost(J, I);
		journalpost.setJournalDato(Date.valueOf(LOCAL_DATE_TIME.toLocalDate()));

		SimpleDateFormat datoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String formatedJournalDato = datoFormat.format(journalpost.getJournalDato());

		var exception = assertThrows(InputValideringFeiletException.class,
				() -> validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost));
		assertThat(exception.getMessage()).contains(
				format("AvsenderMottaker.Id kan ikke oppdateres da journalposten er journalført for over 1 år siden. journalDato=%s", formatedJournalDato),
				format("AvsenderMottaker.Navn kan ikke oppdateres da journalposten er journalført for over 1 år siden. journalDato=%s", formatedJournalDato),
				format("Tittel kan ikke oppdateres da journalposten er journalført for over 1 år siden. journalDato=%s", formatedJournalDato));
	}
}