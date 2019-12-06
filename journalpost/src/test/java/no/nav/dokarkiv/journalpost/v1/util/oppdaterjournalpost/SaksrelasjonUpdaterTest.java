package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_FAGSYSTEM;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_SAKID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestSak;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SaksrelasjonUpdaterTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@InjectMocks
	private SaksrelasjonUpdater updater;

    @Test
    public void shouldUpdateSaksrelasjon() throws UgyldigAksjonsLoggException {
        OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

        Journalpost journalpost = TestUtils.createJournalpost();

        ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, journalpost.getSaksrelasjon().getSakId());

        assertThat(journalpost.getSaksrelasjon().getSakId(), is(oppdaterJournalpostRequest.getSak().getArkivsaksnummer()));
        assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
        assertThat(changeTracker.getChanges(), hasSize(0));
    }

    @Test
    public void shouldUpdateSaksrelasjonWhenSaksrelasjonIsNull() {
        OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

        Journalpost journalpost = TestUtils.createJournalpost();
        journalpost.setSaksrelasjon(null);

        ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, null);

        assertThat(journalpost.getSaksrelasjon().getSakId(), is(oppdaterJournalpostRequest.getSak().getArkivsaksnummer()));
        assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
        assertThat(changeTracker.getChanges(), hasSize(2));
        assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_SAKID).fraVerdi(null).tilVerdi(SAK_ID).build()));
        assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_FAGSYSTEM).fraVerdi(null).tilVerdi(FagsystemCode.FS22.name()).build()));
    }

	@Test
	public void shouldUpdateSaksrelasjonIfFagSakAndFagsaksystemIsPP01() {
		Sak createSak = Sak.builder()
				.fagsakId(FAGSAK_ID)
				.sakstype(Sakstype.FAGSAK)
				.fagsaksystem(Fagsaksystem.PP01)
				.build();

		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestSak(createSak);
		Journalpost journalpost = TestUtils.createJournalpost();

		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, null);
		assertEquals(journalpost.getSaksrelasjon().getFagsystem(), FagsystemCode.PEN);
	}

	@Test
	public void shouldNotUpdateSaksrelasjonIfGeneralSakAndFagsaksystemIsNotPP01() {
		Sak createSak = Sak.builder()
				.fagsakId(FAGSAK_ID)
				.sakstype(Sakstype.GENERELL_SAK)
				.fagsaksystem(Fagsaksystem.BISYS)
				.build();

		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestSak(createSak);
		Journalpost journalpost = TestUtils.createJournalpost();
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("Kan ikke oppdatere sakId basert på input");
		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, null);
		assertEquals(journalpost.getSaksrelasjon().getFagsystem(), FagsystemCode.FS22);
	}

	@Test
	public void shouldUpdateSaksrelasjonWhenSaksrelasjonSakIdIsNull() {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

        Journalpost journalpost = TestUtils.createJournalpost();
        journalpost.getSaksrelasjon().setSakId(null);

        ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, journalpost.getSaksrelasjon().getSakId());

        assertThat(journalpost.getSaksrelasjon().getSakId(), is(oppdaterJournalpostRequest.getSak().getArkivsaksnummer()));
        assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
        assertThat(changeTracker.getChanges(), hasSize(1));
        assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_SAKID).fraVerdi(null).tilVerdi(SAK_ID).build()));
    }

    @Test
    public void shouldUpdateSaksrelasjonWhenSaksrelasjonFagsystemIsNull() {
        OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

        Journalpost journalpost = TestUtils.createJournalpost();
        journalpost.getSaksrelasjon().setFagsystem(null);

        ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, null);

        assertThat(journalpost.getSaksrelasjon().getSakId(), is(oppdaterJournalpostRequest.getSak().getArkivsaksnummer()));
        assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
        assertThat(changeTracker.getChanges(), hasSize(1));
        assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_FAGSYSTEM).fraVerdi(null).tilVerdi(FagsystemCode.FS22.name()).build()));
    }
}
