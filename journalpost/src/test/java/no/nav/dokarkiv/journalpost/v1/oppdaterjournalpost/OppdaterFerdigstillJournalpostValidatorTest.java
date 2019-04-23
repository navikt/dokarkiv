package no.nav.dokarkiv.journalpost.v1.oppdaterjournalpost;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBrukerPerson;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createSak;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OppdaterFerdigstillJournalpostValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OppdaterJournalpostRequest oppdaterJournalpostRequest;

    @Test
    public void happyPath() {
        oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.M);
    }

    @Test
    public void shouldFailIfBrukerSetForStatusJ() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
                .bruker(createBrukerPerson())
                .build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.J);
    }

    @Test
    public void shouldFailIfSakSetForStatusJ() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
                .sak(createSak())
                .build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.J);
    }

    @Test
    public void shouldFailIfTemaSetForStatusJ() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().tema(TEMA_FOR).build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.J);
    }

    @Test
    public void shouldFailIfBrukerSetForStatusFS() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
                .bruker(createBrukerPerson())
                .build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS);
    }

    @Test
    public void shouldFailIfSakSetForStatusFS() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
                .sak(createSak())
                .build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS);
    }

    @Test
    public void shouldFailIfTemaSetForStatusFS() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder().tema(TEMA_FOR).build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS);
    }

    @Test
    public void shouldFailIfAvsenderMottakerSetForStatusFS() {
        oppdaterJournalpostRequest = OppdaterJournalpostRequest.builder()
                .avsenderMottaker(createAvsenderMottakerPerson())
                .build();
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(oppdaterJournalpostRequest, JournalStatusCode.FS);
    }
}