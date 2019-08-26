package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_FAGSYSTEM;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_SAKID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SaksrelasjonUpdaterTest {

    @InjectMocks
    private SaksrelasjonUpdater updater;

    @Test
    public void shouldUpdateSaksrelasjon() throws UgyldigAksjonsLoggException {
        OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

        Journalpost journalpost = TestUtils.createJournalpost();

        ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest);

        assertThat(journalpost.getSaksrelasjon().getSakId(), is(oppdaterJournalpostRequest.getSak().getArkivsaksnummer()));
        assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
        assertThat(changeTracker.getChanges(), hasSize(0));
    }

    @Test
    public void shouldUpdateSaksrelasjonWhenSaksrelasjonIsNull() {
        OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

        Journalpost journalpost = TestUtils.createJournalpost();
        journalpost.setSaksrelasjon(null);

        ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest);

        assertThat(journalpost.getSaksrelasjon().getSakId(), is(oppdaterJournalpostRequest.getSak().getArkivsaksnummer()));
        assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
        assertThat(changeTracker.getChanges(), hasSize(2));
        assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_SAKID).fraVerdi(null).tilVerdi(SAK_ID).build()));
        assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_FAGSYSTEM).fraVerdi(null).tilVerdi(Arkivsaksystem.GSAK.name()).build()));
    }

    @Test
    public void shouldUpdateSaksrelasjonWhenSaksrelasjonSakIdIsNull() {
        OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

        Journalpost journalpost = TestUtils.createJournalpost();
        journalpost.getSaksrelasjon().setSakId(null);

        ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest);

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

        ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest);

        assertThat(journalpost.getSaksrelasjon().getSakId(), is(oppdaterJournalpostRequest.getSak().getArkivsaksnummer()));
        assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
        assertThat(changeTracker.getChanges(), hasSize(1));
        assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_FAGSYSTEM).fraVerdi(null).tilVerdi(Arkivsaksystem.GSAK.name()).build()));
    }
}
