package no.nav.dokarkiv.behandlejournal.v3.tjoark064;

/**
 * Interface for the operation journalfoerUtgaaendeHenvendelse
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
public interface JournalfoerUtgaaendeHenvendelse {
	/**
	 * Validates, updates mandatory values and persists the Journalpost contained within the request.
	 * 
	 * @param journalfoerUtgaaendeHenvendelseRequest The request containing the Journalpost to create.
	 * @return The response object containing the persisted journalpostId and dokumentId.
	 */
	JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest journalfoerUtgaaendeHenvendelseRequest);
}
