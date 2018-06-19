package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.sporing.SporingPopulator;

import javax.inject.Inject;

/**
 * Implementation of AvbrytJournalpostUpdater
 *
 * @author Stig Strøm
 */
public class DefaultAvbrytJournalpostUpdater implements AvbrytJournalpostUpdater {

	@Inject
	private SporingPopulator sporingPopulator;

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
