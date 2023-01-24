package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseResponse;

/**
 * Defines the Joark MOD information service BehandleJournal.
 */
public interface BehandleJournalV3ServiceBi {
	/**
	 * Creates a notat Journalpost with a main document.
	 *
	 * @param journalfoerNotatHenvendelseRequest The request containing the Journalpost to create.
	 * @return The response containing the journalpostId and dokumentId of the
	 * created Journalpost and document.
	 */
	JournalfoerNotatHenvendelseResponse journalfoerNotatHenvendelse(
			JournalfoerNotatHenvendelseRequest journalfoerNotatHenvendelseRequest);
}
