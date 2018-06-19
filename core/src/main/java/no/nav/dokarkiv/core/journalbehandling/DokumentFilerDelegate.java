package no.nav.dokarkiv.core.journalbehandling;

import no.nav.domain.dok.joark.Journalpost;

/**
 * Delegate that encapsulates logic for handling DokumentFil.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface DokumentFilerDelegate {

	/**
	 * Saves new DokumentFiler and updates existing ones based on the input Journalpost.
	 * 
	 * @param journalpost The Journalpost containing the updates.
	 */
	void saveUpdateDokumentFiler(Journalpost journalpost);
	
}
