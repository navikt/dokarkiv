package no.nav.dokarkiv.journalpost.v1.rjoark200;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SaksrelasjonUpdaterTest {
    @Mock
    private BrukerRepository brukerRepositoryMock;
    @Mock
    private AksjonsLoggService aksjonsLoggService;

    private OppdaterJournalpostRequest oppdaterJournalpostRequest;
    private Journalpost journalpost;

    @InjectMocks
    private SaksrelasjonUpdater updater;


    @Test
    public void shouldUpdateSaksrelasjon() throws UgyldigAksjonsLoggException {
        oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

        journalpost = TestUtils.createJournalpost();

        updater.updateFields(journalpost, oppdaterJournalpostRequest);

        assertThat(journalpost.getSaksrelasjon().getSakId(), is(oppdaterJournalpostRequest.getSak().getArkivsaksnummer()));
        assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
    }

}
