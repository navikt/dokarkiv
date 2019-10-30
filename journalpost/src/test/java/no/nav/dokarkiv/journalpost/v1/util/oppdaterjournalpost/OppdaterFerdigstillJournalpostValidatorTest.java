package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.ARKIVSAKSNUMMER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTINFO_ID1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.LOCAL_DATE_TIME;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBrukerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createSak;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
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
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.build())
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
	public void shouldThrowExceptionWhenTemaPenForGenerellSak() {
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).fagsakId(FAGSAK_ID).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("tema");
		OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M, JournalpostTypeCode.I);
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForArkivsak(){
		oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.sakstype(Sakstype.ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(Arkivsaksystem.GSAK)
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
                        .arkivsaksystem(Arkivsaksystem.GSAK)
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



}