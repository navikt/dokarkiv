package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.LOCAL_DATE_TIME;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBrukerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createSak;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Date;

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
    public void shouldFailIfAvsenderMottakerNavnOrIdSetForStatusFS() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
                .avsenderMottaker(createAvsenderMottakerPerson())
                .build();
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