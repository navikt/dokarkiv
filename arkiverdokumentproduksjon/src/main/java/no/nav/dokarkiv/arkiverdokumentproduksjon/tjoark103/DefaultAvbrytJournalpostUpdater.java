package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.domain.dok.joark.codestable.DokumentStatusCode;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.service.dok.joark.journalbehandling.SporingPopulator;
import no.nav.service.dok.joark.nsb.AvbrytJournalpostUpdater;

import javax.inject.Inject;

/**
 * Implementation of AvbrytJournalpostUpdater
 * 
 * @author Stig Strøm
 *
 */
public class DefaultAvbrytJournalpostUpdater implements AvbrytJournalpostUpdater {
	
	@Inject
	SporingPopulator sporingPopulator;

	public void setSporingPopulator(SporingPopulator sporingPopulator) {
		this.sporingPopulator = sporingPopulator;
	}

	@Override
	public Journalpost updateJournalpost(Journalpost journalpost, String endretAvNavn) {
		journalpost.setJournalstatus(JournalStatusCode.A);
		
		for (JournalpostDokumentInfoRelasjon journalpostDokInfoRel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			updateDokumentInfo(journalpostDokInfoRel.getDokumentInfo());
		}
		sporingPopulator.populateSporingInfo(journalpost, endretAvNavn);
		return journalpost;

	}
	
	private void updateDokumentInfo(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getDokumentstatus() == DokumentStatusCode.UNDER_REDIGERING) {
			dokumentInfo.setDokumentstatus(DokumentStatusCode.AVBRUTT);
		}
	}
	
}
