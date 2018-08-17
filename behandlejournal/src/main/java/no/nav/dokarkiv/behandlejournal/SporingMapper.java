package no.nav.dokarkiv.behandlejournal;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Defines mapper that maps sporing information.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface SporingMapper {

	/**
	 * Maps sporing information.
	 * 
	 * @param journalpost The journalpost to process.
	 * @param endretAvNavn The endreAvNavn from the service request.
	 */
	void mapSporingsinfo(Journalpost journalpost, String endretAvNavn);
}
