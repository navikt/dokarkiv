package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

/**
 * Interface for the operation journalfoerNotatHenvendelse
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface JournalfoerNotatHenvendelse {
	/**
	 * Validates, updates mandatory values and persists the Journalpost contained within the request.
	 *
	 * @param journalfoerNotatHenvendelseRequest The request containing the Journalpost to create.
	 * @return The response object containing the persisted journalpostId and dokumentId.
	 */
	JournalfoerNotatHenvendelseResponse journalfoerNotatHenvendelse(
			JournalfoerNotatHenvendelseRequest journalfoerNotatHenvendelseRequest);
}
