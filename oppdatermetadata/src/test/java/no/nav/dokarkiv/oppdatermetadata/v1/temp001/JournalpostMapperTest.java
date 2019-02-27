package no.nav.dokarkiv.oppdatermetadata.v1.temp001;

import static no.nav.dokarkiv.oppdatermetadata.v1.util.TestUtils.createPutOppdaterMetadataRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataRequest;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.oppdatermetadata.v1.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JournalpostMapperTest {

	@Mock
	private BrukerRepository brukerRepositoryMock;

	private PutOppdatermetadataRequest putOppdatermetadataRequest;
	private Journalpost journalpost;

	@InjectMocks
	private JournalpostMapper mapper = new JournalpostMapper();


	@Test
	public void shouldUpdateJournalpost() {
		putOppdatermetadataRequest = createPutOppdaterMetadataRequest();

		journalpost = TestUtils.createJournalpost();

		assertThat(journalpost.getBrukere(), hasSize(2));

		mapper.oppdaterJournalpost(journalpost, putOppdatermetadataRequest);

		assertThat(journalpost.getFagomrade().name(), is(putOppdatermetadataRequest.getTema()));
		assertThat(journalpost.getInnhold(), is(putOppdatermetadataRequest.getTittel()));
		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvEksisterende() {
		putOppdatermetadataRequest = createPutOppdaterMetadataRequest();

		journalpost = TestUtils.createJournalpostForOppdatering();

		mapper.oppdaterJournalpost(journalpost, putOppdatermetadataRequest);

		assertThat(journalpost.getBrukere(), hasSize(1));
	}
}