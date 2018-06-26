package no.nav.dokarkiv.core.journabehandling;


import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Implementation of JournalpostStructureVerifier.
 *
 * @author Thomas Eugen Bj�rge, Visma Sirius
 */
public class DefaultJournalpostStructureVerifier implements JournalpostStructureVerifier {

	/**
	 * {@inheritDoc}
	 */
	public void verifyJournalpostStructure(Journalpost journalpost) {
		journalpost.verifyUniqueDokumentInfoRelasjoner();
		journalpost.verifyNoDokumentVariantDuplicates();
		journalpost.verifyStructureForEndeligJournalforing();
	}

}
