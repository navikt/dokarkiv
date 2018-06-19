package no.nav.dokarkiv.core.sporing;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;

/**
 * Implementation of SporingPopulator.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultSporingPopulator implements SporingPopulator {

	private KildeNavnPopulator kildeNavnPopulator;
	
	/** {@inheritDoc} */
	@Override
	public void populateSporingInfo(Journalpost journalpost, String opprettetEndretNavn) {
		populateOpprettetEndretAvNavn(journalpost, opprettetEndretNavn);
		
		String kildeNavn = RequestContextHolder.currentRequestContext().getComponentId();
		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(journalpost, kildeNavn);		
	}

	private void populateOpprettetEndretAvNavn(Journalpost journalpost, String navn) {
		if (journalpost.hasId()) {
			journalpost.setEndretAvNavn(navn);
		} else {
			journalpost.setOpprettetAvNavn(navn);
		}
		if (journalpost.getSaksrelasjon() != null && journalpost.getSaksrelasjon().hasId()) {
			journalpost.getSaksrelasjon().setEndretAvNavn(navn);
		}
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			if (dokumentInfo.hasId()) {
				dokumentInfo.setEndretAvNavn(navn);
			}
		}
	}

	/**
	 * Setter for the kildeNavnPopulator property.
	 *
	 * @param kildeNavnPopulator the kildeNavnPopulator to set
	 */
	public void setKildeNavnPopulator(KildeNavnPopulator kildeNavnPopulator) {
		this.kildeNavnPopulator = kildeNavnPopulator;
	}

}