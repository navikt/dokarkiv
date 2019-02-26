package no.nav.dokarkiv.oppdatermetadata.v1.support;

import static no.nav.dokarkiv.oppdatermetadata.v1.util.TestUtils.createJournalpostForOppdatering;
import static no.nav.dokarkiv.oppdatermetadata.v1.util.TestUtils.createPutOppdaterMetadataRequest;

import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataRequest;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OppdaterMetadataValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PutOppdatermetadataRequest putOppdatermetadataRequest;

    @Before
    public void setUp() throws Exception {
        putOppdatermetadataRequest = createPutOppdaterMetadataRequest();
    }

    @Test
    public void happyPath() {
        OppdaterMetadataValidator.validateOppdaterteFelt(putOppdatermetadataRequest, JournalStatusCode.M);
    }

    @Test
    public void shouldFailIfOppdateringUlovligForStatus() {
        expectedException.expect(InputValideringFeiletException.class);
        OppdaterMetadataValidator.validateOppdaterteFelt(putOppdatermetadataRequest, JournalStatusCode.J);
    }

}