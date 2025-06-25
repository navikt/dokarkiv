package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;

import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_MOTTAKER_UTLAND;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DATO_MOTTATT_1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.MOTTAT_DATO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottaker;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostAvsenderMottakerKunLandRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestUtenDatoMottat;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithAvsenderMottaker;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithDatoMottat;
import static no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater.DELETE_MARKER;
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
	public void shouldRemoveAvsenderMottakerIdType() throws UgyldigAksjonsLoggException {
		MDC.put(MDC_USER_NAME, "Test Testesen");
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(createAvsenderMottaker(null, DELETE_MARKER, AvsenderMottakerIdType.FNR));

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals("Test Testesen", journalpost.getEndretAvNavn());
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
		assertNull(journalpost.getAvsenderMottakerId());
		assertNull(journalpost.getAvsenderMottakerIdType());
	}

	@Test
	public void shouldRemoveAvsenderMottakerNavn() throws UgyldigAksjonsLoggException {
		MDC.put(MDC_USER_NAME, "Test Testesen");
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(createAvsenderMottaker(DELETE_MARKER, null, null));

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals("Test Testesen", journalpost.getEndretAvNavn());
		assertNull(journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		assertNull(journalpost.getAvsenderMottakerIdType());
	}

	@Test
	public void shouldNotChangeAvsenderMottaker() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(null);

		journalpost = TestUtils.createJournalpostForOppdatering();

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
	}

	@Test
	public void shouldNotChangeAvsenderMottakerIdWithWrongDeleteMarker() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(createAvsenderMottaker(AVSENDER_NAVN, "", AvsenderMottakerIdType.FNR));

		journalpost = TestUtils.createJournalpostForOppdatering();
		assertNull(journalpost.getAvsenderMottakerIdType());

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AvsenderMottakerIdTypeCode.FNR, journalpost.getAvsenderMottakerIdType());
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

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvLandEksisterende() throws UgyldigAksjonsLoggException {
		oppdaterJournalpostRequest = createPutOppdaterJournalpostAvsenderMottakerKunLandRequest();

		journalpost = TestUtils.createJournalpostForOppdatering();

		assertNull(journalpost.getLand());

		updater.updateFields(journalpost, oppdaterJournalpostRequest);

		assertThat(journalpost.getBrukere(), hasSize(1));
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER_MOTTAKER_UTLAND, journalpost.getLand());
	}
}