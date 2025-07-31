package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.consumer.ereg.EregConsumer;
import no.nav.dokarkiv.core.consumer.ereg.EregResponse;
import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_AVSENDER_MOTTAKER;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_AVSENDER_MOTTAKER_ID_TYPE;
import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_MOTTAKER_UTLAND;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottaker;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerOrganisasjonWithoutNavn;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createEregResponse;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostAvsenderMottakerKunLandRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestWithAvsenderMottaker;
import static no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.AvsenderMottakerUpdater.DELETE_MARKER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvsenderMottakerUpdaterTest {
	private static final String NY_AVSENDER_MOTTAKER_ID = "11223344556";
	private static final String NY_AVSENDER_MOTTAKER_NAVN = "Max Mekker";

	private static final String NY_AVSENDER_MOTTAKER_ORG_NAVN = "Max Mekker AS";

	@Mock
	private IdentConsumer identConsumerMock;
	@Mock
	private EregConsumer eregConsumerMock;
	@InjectMocks
	private AvsenderMottakerUpdater avsenderMottakerUpdater;

	@Test
	void shouldChangeAvsenderMottakerId() {
		when(identConsumerMock.hentPersonnavn(eq(NY_AVSENDER_MOTTAKER_ID))).thenReturn(NY_AVSENDER_MOTTAKER_NAVN);
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(createAvsenderMottaker(null, NY_AVSENDER_MOTTAKER_ID, FNR));

		Journalpost journalpost = TestUtils.createJournalpostForOppdatering();
		ChangeTracker changeTracker = new ChangeTracker();

		avsenderMottakerUpdater.updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, changeTracker);

		assertThat(journalpost.getAvsenderMottaker()).isEqualTo(NY_AVSENDER_MOTTAKER_NAVN);
		assertThat(journalpost.getAvsenderMottakerId()).isEqualTo(NY_AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getAvsenderMottakerIdType()).isEqualTo(AvsenderMottakerIdTypeCode.FNR);
		assertThat(changeTracker.getChanges()).hasSize(3);
		assertThat(changeTracker.isEndretFlagg()).isTrue();
		assertThat(changeTracker.getChanges())
				.extracting(ArkivElementEndringTO::getArkivElement, ArkivElementEndringTO::getFraVerdi, ArkivElementEndringTO::getTilVerdi)
				.containsExactlyInAnyOrderElementsOf(List.of(
						tuple(JOURNALPOST_AVSENDER_MOTTAKER, AVSENDER_NAVN, NY_AVSENDER_MOTTAKER_NAVN),
						tuple(JOURNALPOST_AVSENDER_MOTTAKER_ID, AVSENDER_ID_PERSON, NY_AVSENDER_MOTTAKER_ID),
						tuple(JOURNALPOST_AVSENDER_MOTTAKER_ID_TYPE, null, AvsenderMottakerIdTypeCode.FNR.toString())
				));
	}

	@Test
	public void shouldRemoveAvsenderMottakerIdAndIdType() throws UgyldigAksjonsLoggException {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(createAvsenderMottaker(null, DELETE_MARKER, FNR, null));

		Journalpost journalpost = TestUtils.createJournalpostForOppdatering();
		journalpost.setAvsenderMottakerIdType(AvsenderMottakerIdTypeCode.FNR);
		ChangeTracker changeTracker = new ChangeTracker();

		avsenderMottakerUpdater.updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, changeTracker);

		assertThat(journalpost.getAvsenderMottaker()).isEqualTo(AVSENDER_NAVN);
		assertThat(journalpost.getAvsenderMottakerId()).isNull();
		assertThat(journalpost.getAvsenderMottakerIdType()).isNull();
		assertThat(changeTracker.getChanges()).hasSize(2);
		assertThat(changeTracker.isEndretFlagg()).isTrue();
		assertThat(changeTracker.getChanges())
				.extracting(ArkivElementEndringTO::getArkivElement, ArkivElementEndringTO::getFraVerdi, ArkivElementEndringTO::getTilVerdi)
				.containsExactlyInAnyOrderElementsOf(List.of(
						tuple(JOURNALPOST_AVSENDER_MOTTAKER_ID, AVSENDER_ID_PERSON, null),
						tuple(JOURNALPOST_AVSENDER_MOTTAKER_ID_TYPE, AvsenderMottakerIdTypeCode.FNR.toString(), null)
				));
	}

	@Test
	public void shouldRemoveAvsenderMottakerNavn() throws UgyldigAksjonsLoggException {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(createAvsenderMottaker(DELETE_MARKER, null, null, null));

		Journalpost journalpost = TestUtils.createJournalpostForOppdatering();
		ChangeTracker changeTracker = new ChangeTracker();

		avsenderMottakerUpdater.updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, changeTracker);

		assertThat(journalpost.getAvsenderMottaker()).isNull();
		assertThat(journalpost.getAvsenderMottakerId()).isEqualTo(AVSENDER_ID_PERSON);
		assertNull(journalpost.getAvsenderMottakerIdType());
		assertThat(changeTracker.getChanges()).hasSize(1);
		assertThat(changeTracker.isEndretFlagg()).isTrue();
		assertThat(changeTracker.getChanges())
				.extracting(ArkivElementEndringTO::getArkivElement, ArkivElementEndringTO::getFraVerdi, ArkivElementEndringTO::getTilVerdi)
				.containsExactlyInAnyOrderElementsOf(List.of(
						tuple(JOURNALPOST_AVSENDER_MOTTAKER, AVSENDER_NAVN, null)
				));
	}

	@Test
	public void shouldNotChangeAvsenderMottaker() throws UgyldigAksjonsLoggException {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(null);

		Journalpost journalpost = TestUtils.createJournalpostForOppdatering();
		ChangeTracker changeTracker = new ChangeTracker();

		avsenderMottakerUpdater.updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, changeTracker);

		assertThat(journalpost.getAvsenderMottaker()).isEqualTo(AVSENDER_NAVN);
		assertThat(journalpost.getAvsenderMottakerId()).isEqualTo(AVSENDER_ID_PERSON);
		assertThat(journalpost.getAvsenderMottakerIdType()).isNull();
		assertThat(changeTracker.getChanges()).isEmpty();
		assertThat(changeTracker.isEndretFlagg()).isFalse();
	}

	@Test
	public void shouldNotChangeAvsenderMottakerIdWithWrongDeleteMarker() throws UgyldigAksjonsLoggException {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(createAvsenderMottaker(null, "", null, null));

		Journalpost journalpost = TestUtils.createJournalpostForOppdatering();
		ChangeTracker changeTracker = new ChangeTracker();

		avsenderMottakerUpdater.updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, changeTracker);

		assertThat(journalpost.getAvsenderMottaker()).isEqualTo(AVSENDER_NAVN);
		assertThat(journalpost.getAvsenderMottakerId()).isEqualTo(AVSENDER_ID_PERSON);
		assertThat(journalpost.getAvsenderMottakerIdType()).isNull();
		assertThat(changeTracker.getChanges()).isEmpty();
		assertThat(changeTracker.isEndretFlagg()).isFalse();
	}

	@Test
	public void shouldNotClearBrukerListeVedOppdateringAvLandEksisterende() throws UgyldigAksjonsLoggException {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostAvsenderMottakerKunLandRequest();

		Journalpost journalpost = TestUtils.createJournalpostForOppdatering();

		assertNull(journalpost.getLand());

		ChangeTracker changeTracker = new ChangeTracker();
		avsenderMottakerUpdater.updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, changeTracker);

		assertThat(journalpost.getBrukere()).hasSize(1);
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER_MOTTAKER_UTLAND, journalpost.getLand());
	}

	@Test
	void shouldChangeAvsenderMottakerNavnForOrganisasjon() {
		EregResponse eregResponse = createEregResponse(AVSENDER_ID_ORGANISASJON, NY_AVSENDER_MOTTAKER_ORG_NAVN);
		when(eregConsumerMock.hentOrganisasjonsnavn(eq(AVSENDER_ID_ORGANISASJON))).thenReturn(eregResponse);

		AvsenderMottaker avsenderMottaker = createAvsenderMottakerOrganisasjonWithoutNavn();

		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestWithAvsenderMottaker(avsenderMottaker);

		Journalpost journalpost = TestUtils.createJournalpostForOppdatering();
		ChangeTracker changeTracker = new ChangeTracker();

		avsenderMottakerUpdater.updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, changeTracker);

		assertThat(journalpost.getAvsenderMottaker()).isEqualTo(NY_AVSENDER_MOTTAKER_ORG_NAVN);
	}

}