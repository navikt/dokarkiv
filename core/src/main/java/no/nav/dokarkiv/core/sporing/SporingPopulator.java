package no.nav.dokarkiv.core.sporing;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Populates sporingsinfo, opprettetNavn/EndretNavn and opprettetKildeNavn/endretKildeNavn.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface SporingPopulator {

	/**
	 * Populates sporingsinfo.
	 * 
	 * @param journalpost The Journalpost to update.
	 * @param opprettetEndretNavn The name used to update sporingsinfo with.
	 */
	void populateSporingInfo(Journalpost journalpost, String opprettetEndretNavn);
	
}
