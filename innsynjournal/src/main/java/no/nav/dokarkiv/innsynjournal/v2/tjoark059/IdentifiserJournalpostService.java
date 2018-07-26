package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostNotSupportedException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.UgyldigInputException;

/**
 * Interface for the innsyn operation identifiserJournalpost (TJOARK059)
 *
 * @author Ketill Fenne, Visma Consulting
 */
public interface IdentifiserJournalpostService {

	/**
	 * Henter ut journalpost knyttet til kanalreferanse og mottakskanal
	 *
	 * @param identifiserJournalpostToRequest
	 * @return journapost, Hoveddokument og liste over vedlegg
	 */
	Journalpost identifiserJournalpost(IdentifiserJournalpostToRequest identifiserJournalpostToRequest) throws JournalpostNotSupportedException, JournalpostIkkeFunnetException, UgyldigInputException, JournalpostIkkeInngaaendeException;
}
