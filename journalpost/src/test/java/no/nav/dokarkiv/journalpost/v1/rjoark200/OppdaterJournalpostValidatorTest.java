package no.nav.dokarkiv.oppdaterjournalpost.v1.support;

import static no.nav.dokarkiv.oppdaterjournalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;

import no.nav.dok.oppdaterjournalpost.api.v1.PutOppdaterJournalpostRequest;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OppdaterJournalpostValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PutOppdaterJournalpostRequest putOppdaterJournalpostRequest;

    @Before
    public void setUp() throws Exception {
        putOppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();
    }

    @Test
    public void happyPath() {
        OppdaterJournalpostValidator.validateOppdaterteFelt(putOppdaterJournalpostRequest, JournalStatusCode.M);
    }

    @Test
    public void shouldFailIfOppdateringUlovligForStatus() {
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterJournalpostValidator.validateOppdaterteFelt(putOppdaterJournalpostRequest, JournalStatusCode.J);
    }

}