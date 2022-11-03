package no.nav.dokarkiv.core.journalbehandling;


import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Delegate that encapsulates logic for handling DokumentFil.
 */
public interface DokumentFilerDelegate {

	/**
	 * Saves new DokumentFiler and updates existing ones based on the input Journalpost.
	 * 
	 * @param journalpost The Journalpost containing the updates.
	 */
	void saveUpdateDokumentFiler(Journalpost journalpost);


	/**
	 * Only saves new DokumentFiler
	 *
	 * @param journalpost The Journalpost containing the updates.
	 */
	void saveNewDokumentFiler(Journalpost journalpost);



}
