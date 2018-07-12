package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

/**
 * Mapper for JournalfoerInngaaendeHenvendelseRequest from FIM
 * to joark domain request.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public interface JournalfoerInngaaendeHenvendelseRequestMapper {

	/**
	 * Map for JournalfoerInngaaendeHenvendelse WS request to
	 * domain request
	 * 
	 * @param wsRequest
	 *            the JournalfoerInngaaendeHenvendelseRequest ws
	 *            object
	 * @return the domain
	 *         JournalfoerInngaaendeHenvendelseRequest object
	 */
	JournalfoerInngaaendeHenvendelseRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseRequest wsRequest);
}
