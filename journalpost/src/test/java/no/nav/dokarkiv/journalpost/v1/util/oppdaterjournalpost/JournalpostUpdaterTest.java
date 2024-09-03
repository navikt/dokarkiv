package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
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
import org.slf4j.MDC;

import java.util.Date;

import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_MOTTAKER_UTLAND;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DATO_MOTTATT_1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.MOTTAT_DATO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostAvsenderMottakerKunLandRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestUtenDatoMottat;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithDatoMottat;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithoutAvsenderMottaker;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithoutAvsenderMottakerId;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithoutWrongAvsenderMottakerId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
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
		journalpost = TestUtils.createEnkelJournalpost();

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

		journalpost = TestUtils.createEnkelJournalpost();
		assertNull(journalpost.getAntallRetur());

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(1, journalpost.getAntallRetur().intValue());
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(1, journalpost.getAntallRetur().intValue());

		earliest.setTime(earliest.getTime() + 1);
		oppdaterJournalpostRequest = TestUtils.createPutOppdaterJournalpostRequestWithDatoRetur(earliest);
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(2, journalpost.getAntallRetur().intValue());
		updater.updateFields(journalpost, oppdaterJournalpostRequest);
		assertEquals(2, journalpost.getAntallRetur().intValue());
	}


	@Test
	public void shouldRemoveAvsenderMottakerIdType() throws UgyldigAksjonsLoggException {
		MDC.put(MDC_USER_NAME, "Test Testesen");
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithoutAvsenderMottakerId();

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals("Test Testesen", journalpost.getEndretAvNavn());
		assertNull(journalpost.getAvsenderMottakerId());
		assertNull(journalpost.getAvsenderMottakerIdType());
	}

	@Test
	public void shouldNotChangeAvsenderMottaker() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithoutAvsenderMottaker();

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(journalpost.getAvsenderMottakerId(), AVSENDER_ID_PERSON);
		assertEquals(journalpost.getAvsenderMottaker(), AVSENDER_NAVN);
	}

	@Test
	public void shouldNotChangeAvsenderMottakerIdWithWrongDeleteMarker() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithoutWrongAvsenderMottakerId();

		journalpost = TestUtils.createJournalpostForOppdatering();
		assertNull(journalpost.getAvsenderMottakerIdType());

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(journalpost.getAvsenderMottakerId(), AVSENDER_ID_PERSON);
		assertEquals(journalpost.getAvsenderMottaker(), AVSENDER_NAVN);
		assertEquals(journalpost.getAvsenderMottakerIdType(), AvsenderMottakerIdTypeCode.FNR);
	}
	@Test
	public void shouldUpdateJPMottattDatoWithNullWhenJpErInngaaendeAndRequestMottattDatoNull() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestUtenDatoMottat();
		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(journalpost.getMottattDato(), MOTTAT_DATO);
		assertEquals(journalpost.getJournalposttype(), JournalpostTypeCode.I);
	}

	@Test
	public void shouldUpdateMottattDatoWhenJpErInngaaende() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithDatoMottat(DATO_MOTTATT_1);
		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(journalpost.getMottattDato(), DATO_MOTTATT_1);
		assertEquals(journalpost.getJournalposttype(), JournalpostTypeCode.I);
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