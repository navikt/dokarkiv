package no.nav.dokarkiv.journalpost.v1.rjoark200;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalpost.v1.api.PutOppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JournalpostMapperTest {

	@Mock
	private BrukerRepository brukerRepositoryMock;
	@Mock
	private AksjonsLoggService aksjonsLoggService;

	private PutOppdaterJournalpostRequest putOppdaterJournalpostRequest;
	private Journalpost journalpost;

	@InjectMocks
	private JournalpostMapper mapper = new JournalpostMapper();


	@Test
	public void shouldUpdateJournalpost() throws UgyldigAksjonsLoggException {
		putOppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

		journalpost = TestUtils.createJournalpost();

		assertThat(journalpost.getBrukere(), hasSize(2));

		mapper.oppdaterJournalpost(journalpost, putOppdaterJournalpostRequest);

		assertThat(journalpost.getFagomrade().name(), is(putOppdaterJournalpostRequest.getTema()));
		assertThat(journalpost.getInnhold(), is(putOppdaterJournalpostRequest.getTittel()));
		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvEksisterende() throws UgyldigAksjonsLoggException {
		putOppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

		journalpost = TestUtils.createJournalpostForOppdatering();

		mapper.oppdaterJournalpost(journalpost, putOppdaterJournalpostRequest);

		assertThat(journalpost.getBrukere(), hasSize(1));
	}
}