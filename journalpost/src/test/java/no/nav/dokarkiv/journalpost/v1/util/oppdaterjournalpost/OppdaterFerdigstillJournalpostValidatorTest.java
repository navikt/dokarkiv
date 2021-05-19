package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Date;
import java.util.Collections;

import static no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem.*;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.ARKIVSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.ARKIVSAKSNUMMER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTINFO_ID1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.LOCAL_DATE_TIME;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBrukerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createSak;

public class OppdaterFerdigstillJournalpostValidatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private OppdaterJournalpostRequest oppdaterJournalpostRequest;

	@Test
	public void happyPath() {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}

	@Test
	public void happyPathFagsak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);

	}

	@Test
	public void happyPathGenerellSak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}

	@Test
	public void happyPathArkivsak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(Sakstype.ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(GSAK)
						.build())
				.build();
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}

	@Test
	public void happyPathTemaPEN() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}

	@Test
	public void happyPathTemaUFO() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForFagsak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksnummer");
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);

	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForGenerellSak(){
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).fagsakId(FAGSAK_ID).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.fagsakId");
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForArkivsak(){
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(Sakstype.ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(GSAK)
						.fagsakId(FAGSAK_ID)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.fagsakId");
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}


	// Det skal alltid være liv til å oppdatere avsenderMottaker (id, navn). Se commit.
	@Test
	public void shouldUpdateAvsenderMottakerNavnOrIdSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.build();
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS, JournalpostTypeCode.U);
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
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS, JournalpostTypeCode.U);
	}

	@Test
	public void shouldFailIfBrukerSetForStatusJ() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.bruker(createBrukerPerson())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.J, JournalpostTypeCode.I);
	}

	@Test
	public void shouldFailIfSakSetForStatusJ() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(createSak())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.J, JournalpostTypeCode.I);
	}

    @Test
    public void shouldThrowExceptionWhenSakArkivsaksnummerNotNumericAndJournalstatusM() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
                .sak(Sak.builder()
                        .arkivsaksnummer("quack123")
                        .arkivsaksystem(GSAK)
                        .build())
                .build();
        expectedException.expect(InputValideringFeiletException.class);
        expectedException.expectMessage("Sak.arkivsaksnummer");
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
    }

    @Test
    public void shouldFailIfJournalFoerendeEnhetSetForStatusJ() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().journalfoerendeEnhet(JOURNALFOERENDE_ENHET).build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.J, JournalpostTypeCode.I);
    }

	@Test
	public void shouldFailIfTemaSetForStatusJ() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().tema(TEMA_FOR).build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.J, JournalpostTypeCode.I);
	}

	@Test
	public void shouldFailIfBrukerSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.bruker(createBrukerPerson())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS, JournalpostTypeCode.U);
	}

	@Test
	public void shouldFailIfSakSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(createSak())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS, JournalpostTypeCode.U);
	}

	@Test
	public void shouldFailIfJournalFoerendeEnhetSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().journalfoerendeEnhet(JOURNALFOERENDE_ENHET).build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS, JournalpostTypeCode.U);
	}

	@Test
	public void shouldFailIfTemaSetForStatusFS() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().tema(TEMA_FOR).build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS, JournalpostTypeCode.U);
	}

    @Test
    public void shouldFailIfDatoReturSetForStatusFSAndNotat() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().datoRetur(Date.valueOf(LOCAL_DATE_TIME.toLocalDate())).build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS, JournalpostTypeCode.N);
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
				.bruker(Bruker.builder().id("test999999").idType(BrukerIdType.ORGNR).build())
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("9999999999")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.D, JournalpostTypeCode.I);
	}


	@Test
	public void shouldFailIfBrukerIdIsNull() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).build())
				.tema("DAG")
				.sak(Sak.builder()
						.fagsakId("10695768")
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.D, JournalpostTypeCode.I);
	}



	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRMoreThan9Digits() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder().id("9999999999").idType(BrukerIdType.ORGNR).build())
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("9999999999")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id må være 9 siffer for ORGNR.");
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.D, JournalpostTypeCode.I);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNotNumeric() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("abc11111111")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.D, JournalpostTypeCode.I);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForFnr() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.D, JournalpostTypeCode.I);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForOrgnr() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder()
						.idType(BrukerIdType.ORGNR)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.D, JournalpostTypeCode.I);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForAktoerid() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.build())
				.tema("test")
				.bruker(Bruker.builder()
						.idType(BrukerIdType.AKTOERID)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.D, JournalpostTypeCode.I);
	}

	@Test
	public void shouldFailIfBrukerIsNull() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.bruker(Bruker.builder().build())
				.tema("DAG")
				.sak(Sak.builder()
						.fagsakId("10695768")
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.D, JournalpostTypeCode.I);
	}

	@Test
	public void shouldThrowExceptionWhenInvalidBehandlingstema(){
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_FOR)
				.behandlingstema("bb3333")
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Behandlingstema er ikke på formatet ´ab + 4 siffer´. Behandlingstema er=bb3333");
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}

}