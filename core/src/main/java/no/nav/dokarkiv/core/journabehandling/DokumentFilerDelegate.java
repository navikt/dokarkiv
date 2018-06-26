package no.nav.dokarkiv.core.journabehandling;


import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Delegate that encapsulates logic for handling DokumentFil.
 *
 * @author Thomas Eugen Bj�rge, Visma Sirius
 */
public interface DokumentFilerDelegate {

	/**
	 * Saves new DokumentFiler and updates existing ones based on the input Journalpost.
	 *
	 * @param journalpost The Journalpost containing the updates.
	 */
	void saveUpdateDokumentFiler(Journalpost journalpost);

}
