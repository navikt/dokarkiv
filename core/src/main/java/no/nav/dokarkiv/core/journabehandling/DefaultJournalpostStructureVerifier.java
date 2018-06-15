package no.nav.dokarkiv.core.journabehandling;

import no.nav.domain.dok.joark.Journalpost;

/**
 * Implementation of JournalpostStructureVerifier.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
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
