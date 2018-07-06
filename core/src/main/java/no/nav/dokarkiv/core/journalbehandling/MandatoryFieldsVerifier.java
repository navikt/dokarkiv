package no.nav.dokarkiv.core.journalbehandling;


import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Inteface defining the MandatoryFieldsVerifier used to verify that mandatory
 * fields are set in the domain object graph.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public interface MandatoryFieldsVerifier {

	/**
	 * Verifies that all mandatory fields for a given JournalStatus are set. An
	 * exception is thrown if a mandatory field is null.
	 *
	 * @param journalpost The journalpost to verify.
	 */
	void verifyFields(Journalpost journalpost, boolean verifyJournalForendeEnhetId);

}
