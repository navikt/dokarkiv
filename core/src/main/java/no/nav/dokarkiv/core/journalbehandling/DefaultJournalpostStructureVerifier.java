package no.nav.dokarkiv.core.journalbehandling;


import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

/**
 * Implementation of JournalpostStructureVerifier.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Component
public class DefaultJournalpostStructureVerifier implements JournalpostStructureVerifier {

	public void verifyJournalpostStructure(Journalpost journalpost) {
		journalpost.verifyUniqueDokumentInfoRelasjoner();
		journalpost.verifyNoDokumentVariantDuplicates();
		journalpost.verifyStructureForEndeligJournalforing();
	}
}
