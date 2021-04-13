package no.nav.dokarkiv.core.journalbehandling;


import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorResponse;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorResponseToGrafana;

import java.util.List;

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


	/**
	 * Only saves new DokumentFiler
	 *
	 * @param journalpost The Journalpost containing the updates.
	 */
	void saveNewDokumentFiler(Journalpost journalpost);

	/**
	 * Saves and validates new DokumentFiler and updates existing ones based on the input Journalpost.
	 *
	 * @param journalpost The Journalpost containing the updates.
	 */
	List<PdfValidatorResponseToGrafana> saveUpdateValidateDokumentFiler(Journalpost journalpost);

	/**
	 * Only saves and validates new DokumentFiler
	 *
	 * @param journalpost The Journalpost containing the updates.
	 */
	void saveAndValidateNewDokumentFiler(Journalpost journalpost, List<PdfValidatorResponseToGrafana> responses);

}
