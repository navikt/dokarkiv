package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.AksjonsLoggHelper;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class JournalpostUpdaterTest {
    @Mock
	private BrukerRepository brukerRepositoryMock;

	private OppdaterJournalpostRequest oppdaterJournalpostRequest;
	private Journalpost journalpost;

	@InjectMocks
	private JournalpostUpdater updater;


	@Test
	public void shouldUpdateJournalpost() throws UgyldigAksjonsLoggException {
		AksjonsLoggHelper aksjonsLoggHelper = new AksjonsLoggHelper();
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();
		journalpost = TestUtils.createJournalpost();

		assertThat(journalpost.getBrukere(), hasSize(2));

		updater.updateFields(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelper);

		assertThat(journalpost.getFagomrade().name(), is(oppdaterJournalpostRequest.getTema()));
		assertThat(journalpost.getInnhold(), is(oppdaterJournalpostRequest.getTittel()));
		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvEksisterende() throws UgyldigAksjonsLoggException {
		AksjonsLoggHelper aksjonsLoggHelper = new AksjonsLoggHelper();
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelper);

		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	@Test
	public void shouldNotIncrementAntallReturWhenDateIsEquals() throws UgyldigAksjonsLoggException {
		AksjonsLoggHelper aksjonsLoggHelper = new AksjonsLoggHelper();
		Date earliest = new Date();
		oppdaterJournalpostRequest = TestUtils.createPutOppdaterJournalpostRequestWithDatoRetur(earliest);

		journalpost = TestUtils.createJournalpost();
		assertNull(journalpost.getAntallRetur());

		updater.updateFields(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelper);

		assertEquals(new Integer(1), journalpost.getAntallRetur());
		updater.updateFields(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelper);
		assertEquals(new Integer(1), journalpost.getAntallRetur());

		earliest.setTime(earliest.getTime() + 1);
		oppdaterJournalpostRequest = TestUtils.createPutOppdaterJournalpostRequestWithDatoRetur(earliest);
		updater.updateFields(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelper);
		assertEquals(new Integer(2), journalpost.getAntallRetur());
		updater.updateFields(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelper);
		assertEquals(new Integer(2), journalpost.getAntallRetur());
	}

}