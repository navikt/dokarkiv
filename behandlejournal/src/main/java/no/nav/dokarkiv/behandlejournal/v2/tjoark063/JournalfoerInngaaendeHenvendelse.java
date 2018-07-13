package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

/**
 * Service to journalføre an inngående journalpost.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
public interface JournalfoerInngaaendeHenvendelse {
	
	/**
	 * Operation validates the journalpost as input and persist in db. Returns the journalpostId from the persisted Journalpost.
	 * 
	 * @param journalfoerInngaaendeHenvendelseRequest the request consisting the journalpost to store.
	 * @return a JournalfoerInngaaendeHenvendelseResponse with journalpostId
	 */
	JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(
			JournalfoerInngaaendeHenvendelseRequest journalfoerInngaaendeHenvendelseRequest);
}
