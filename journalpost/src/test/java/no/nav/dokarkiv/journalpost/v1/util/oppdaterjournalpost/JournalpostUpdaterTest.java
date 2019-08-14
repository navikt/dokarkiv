package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_MOTTAKER_UTLAND;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostAvsenderMottakerKunLandRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithoutAvsenderMottaker;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithoutAvsenderMottakerId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

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
import org.slf4j.MDC;

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
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();
		journalpost = TestUtils.createJournalpost();

		assertThat(journalpost.getBrukere(), hasSize(2));

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertThat(journalpost.getFagomrade().name(), is(oppdaterJournalpostRequest.getTema()));
		assertThat(journalpost.getInnhold(), is(oppdaterJournalpostRequest.getTittel()));
		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvEksisterende() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertThat(journalpost.getBrukere(), hasSize(1));
	}

	@Test
	public void shouldNotIncrementAntallReturWhenDateIsEquals() throws UgyldigAksjonsLoggException {
		Date earliest = new Date();
		oppdaterJournalpostRequest = TestUtils.createPutOppdaterJournalpostRequestWithDatoRetur(earliest);

		journalpost = TestUtils.createJournalpost();
		assertNull(journalpost.getAntallRetur());

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(new Integer(1), journalpost.getAntallRetur());
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(new Integer(1), journalpost.getAntallRetur());

		earliest.setTime(earliest.getTime() + 1);
		oppdaterJournalpostRequest = TestUtils.createPutOppdaterJournalpostRequestWithDatoRetur(earliest);
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(new Integer(2), journalpost.getAntallRetur());
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(new Integer(2), journalpost.getAntallRetur());
	}


	@Test
	public void shouldRemoveAvsenderMottakerIdType() throws UgyldigAksjonsLoggException {
		MDC.put(MDC_USER_ID, "testuser");
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithoutAvsenderMottakerId();

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals("testuser", journalpost.getEndretAvNavn());
		assertNull(journalpost.getAvsenderMottakerId());
		assertNull(journalpost.getAvsenderMottakerIdType());
	}

	@Test
	public void shouldNotChangeAvsenderMottaker() throws UgyldigAksjonsLoggException {
		MDC.put(MDC_USER_ID, "testuser");
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithoutAvsenderMottaker();

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(journalpost.getAvsenderMottakerId(), AVSENDER_ID_PERSON);
		assertEquals(journalpost.getAvsenderMottaker(), AVSENDER_NAVN);
	}

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvLandEksisterende() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostAvsenderMottakerKunLandRequest();

		journalpost = TestUtils.createJournalpostForOppdatering();

		assertNull(journalpost.getLand());

		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertThat(journalpost.getBrukere(), hasSize(1));
		assertEquals(journalpost.getAvsenderMottaker(), AVSENDER_NAVN);
		assertEquals(journalpost.getAvsenderMottakerId(), AVSENDER_ID_PERSON);
		assertEquals(journalpost.getLand(), AVSENDER_MOTTAKER_UTLAND);
	}


}