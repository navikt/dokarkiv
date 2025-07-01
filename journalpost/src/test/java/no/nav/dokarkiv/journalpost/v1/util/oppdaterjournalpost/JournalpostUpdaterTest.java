package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DATO_MOTTATT_1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.MOTTAT_DATO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestUtenDatoMottat;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithDatoMottat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JournalpostUpdaterTest {
	@Mock
	private BrukerRepository brukerRepositoryMock;
	@Mock
	private AvsenderMottakerUpdater avsenderMottakerUpdaterMock;

	private OppdaterJournalpostRequest oppdaterJournalpostRequest;
	private Journalpost journalpost;

	@InjectMocks
	private JournalpostUpdater updater;

	@Test
	public void shouldUpdateJournalpost() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();
		journalpost = TestUtils.createEnkelJournalpost();

		assertThat(journalpost.getBrukere(), hasSize(2));

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertThat(journalpost.getFagomrade().name(), is(oppdaterJournalpostRequest.getTema()));
		assertThat(journalpost.getInnhold(), is(oppdaterJournalpostRequest.getTittel()));
		assertThat(journalpost.getBrukere(), hasSize(1));
		verify(avsenderMottakerUpdaterMock).updateAvsenderMottaker(eq(journalpost), eq(oppdaterJournalpostRequest), any(ChangeTracker.class));
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
		LocalDate earliest = LocalDate.of(2025, 4, 23);
		oppdaterJournalpostRequest = TestUtils.createPutOppdaterJournalpostRequestWithDatoRetur(earliest);

		journalpost = TestUtils.createEnkelJournalpost();
		assertNull(journalpost.getAntallRetur());

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(1, journalpost.getAntallRetur().intValue());
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(1, journalpost.getAntallRetur().intValue());

		oppdaterJournalpostRequest = TestUtils.createPutOppdaterJournalpostRequestWithDatoRetur(earliest.plusDays(1));
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(2, journalpost.getAntallRetur().intValue());
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(2, journalpost.getAntallRetur().intValue());
	}

	@Test
	public void shouldUpdateJPMottattDatoWithNullWhenJpErInngaaendeAndRequestMottattDatoNull() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestUtenDatoMottat();
		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(MOTTAT_DATO, journalpost.getMottattDato());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
	}

	@Test
	public void shouldUpdateMottattDatoWhenJpErInngaaende() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithDatoMottat(DATO_MOTTATT_1);
		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(DATO_MOTTATT_1, journalpost.getMottattDato());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
	}
}