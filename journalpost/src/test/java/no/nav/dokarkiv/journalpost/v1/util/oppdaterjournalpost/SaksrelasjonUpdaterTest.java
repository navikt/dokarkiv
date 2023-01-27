package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.MDCConstants;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_FAGSYSTEM;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_SAKID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAK_APPLIKASJON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.PENSJON_FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createPutOppdaterJournalpostRequestSak;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class SaksrelasjonUpdaterTest {

	@InjectMocks
	private SaksrelasjonUpdater updater;

	@Test
	public void shouldUpdateSaksrelasjon() throws UgyldigAksjonsLoggException {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

		Journalpost journalpost = TestUtils.createJournalpost();

		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, SAK_ID);

		assertThat(journalpost.getSaksrelasjon().getSakId(), is(SAK_ID));
		assertThat(journalpost.getSaksrelasjon().getSaknrfk(), is(SAK_ID.toString()));
		assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
		assertThat(changeTracker.getChanges(), hasSize(1));
		assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAK_APPLIKASJON).fraVerdi(null).tilVerdi(FagsystemCode.FS22.name()).build()));
	}

	@Test
	public void shouldUpdateSaksrelasjonWhenSaksrelasjonIsNull() {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

		Journalpost journalpost = TestUtils.createJournalpost();
		journalpost.setSaksrelasjon(null);

		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, null);

		assertThat(journalpost.getSaksrelasjon().getSakId(), is(SAK_ID));
		assertThat(journalpost.getSaksrelasjon().getSaknrfk(), is(SAK_ID.toString()));
		assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
		assertThat(changeTracker.getChanges(), hasSize(3));
		assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_SAKID).fraVerdi(null).tilVerdi(SAK_ID.toString()).build()));
		assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_FAGSYSTEM).fraVerdi(null).tilVerdi(FagsystemCode.FS22.name()).build()));
		assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAK_APPLIKASJON).fraVerdi(null).tilVerdi(FagsystemCode.FS22.name()).build()));
	}

	@Test
	public void shouldUpdateSaksrelasjonIfFagSakAndFagsaksystemIsPP01() {
		Sak createSak = Sak.builder()
				.fagsakId(PENSJON_FAGSAK_ID)
				.sakstype(Sakstype.FAGSAK)
				.fagsaksystem(Fagsaksystem.PP01)
				.build();

		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestSak(createSak);
		Journalpost journalpost = TestUtils.createJournalpost();

		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, null);
		assertEquals(parseLong(PENSJON_FAGSAK_ID), journalpost.getSaksrelasjon().getSakId());
		assertEquals(PENSJON_FAGSAK_ID, journalpost.getSaksrelasjon().getSaknrfk());
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

		assertThrows(UgyldigInputException.class,
				() -> updater.updateFields(journalpost, oppdaterJournalpostRequest, null),
				"Kan ikke oppdatere sakId basert på input");
	}

	@Test
	public void shouldUpdateSaksrelasjonIfFagsakAndFagsaksystemIsA011() {
		Sak createSak = Sak.builder()
				.fagsakId(FAGSAK_ID)
				.sakstype(Sakstype.FAGSAK)
				.fagsaksystem(Fagsaksystem.AO11)
				.build();

		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestSak(createSak);
		Journalpost journalpost = TestUtils.createJournalpost();

		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, SAK_ID);
		assertEquals(SAK_ID, journalpost.getSaksrelasjon().getSakId());
		assertEquals(SAK_ID.toString(), journalpost.getSaksrelasjon().getSaknrfk());
	}

	@Test
	public void shouldUpdateSaksrelasjonWhenSaksrelasjonSakIdIsNull() {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

		Journalpost journalpost = TestUtils.createJournalpost();
		journalpost.getSaksrelasjon().setSakId(null);
		journalpost.getSaksrelasjon().setSaknrfk(null);

		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, null);

		assertThat(journalpost.getSaksrelasjon().getSakId(), is(SAK_ID));
		assertThat(journalpost.getSaksrelasjon().getSaknrfk(), is(SAK_ID.toString()));
		assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
		assertThat(changeTracker.getChanges(), hasSize(2));
		assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_SAKID).fraVerdi(null).tilVerdi(SAK_ID.toString()).build()));
		assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAK_APPLIKASJON).fraVerdi(null).tilVerdi(FagsystemCode.FS22.name()).build()));
	}

	@Test
	public void shouldUpdateSaksrelasjonWhenSaksrelasjonFagsystemIsNull() {
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequest();

		Journalpost journalpost = TestUtils.createJournalpost();
		journalpost.getSaksrelasjon().setFagsystem(null);

		ChangeTracker changeTracker = updater.updateFields(journalpost, oppdaterJournalpostRequest, null);

		assertThat(journalpost.getSaksrelasjon().getSakId(), is(SAK_ID));
		assertThat(journalpost.getSaksrelasjon().getSaknrfk(), is(SAK_ID.toString()));
		assertThat(journalpost.getSaksrelasjon().getFagsystem(), is(updater.mapArkivSakSystemToFagsystemCode(oppdaterJournalpostRequest.getSak().getArkivsaksystem())));
		assertThat(changeTracker.getChanges(), hasSize(2));
		assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAKSRELASJON_FAGSYSTEM).fraVerdi(null).tilVerdi(FagsystemCode.FS22.name()).build()));
		assertThat(changeTracker.getChanges(), hasItem(ArkivElementEndringTO.builder().arkivElement(SAK_APPLIKASJON).fraVerdi(null).tilVerdi(FagsystemCode.FS22.name()).build()));
	}

	@Test
	public void shouldUpdateSaksrelasjonEndretAvNavn() throws UgyldigAksjonsLoggException {
		MDC.put(MDCConstants.MDC_USER_NAME, "Test Testesen");
		Sak createSak = Sak.builder()
				.fagsakId(FAGSAK_ID)
				.sakstype(Sakstype.FAGSAK)
				.fagsaksystem(Fagsaksystem.AO11)
				.build();
		OppdaterJournalpostRequest oppdaterJournalpostRequest = createPutOppdaterJournalpostRequestSak(createSak);

		Journalpost journalpost = TestUtils.createJournalpost();

		updater.updateFields(journalpost, oppdaterJournalpostRequest, SAK_ID);

		assertThat(journalpost.getSaksrelasjon().getEndretAvNavn(), is("Test Testesen"));
	}
}
