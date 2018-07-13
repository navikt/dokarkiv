package no.nav.dokarkiv.behandlejournal.v2.tjoark063;


import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseResponse;

/**
 * Mapper for JournalfoerInngaaendeHenvendelseResponse from
 * joark domain to FIM request.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public interface JournalfoerInngaaendeHenvendelseResponseMapper {

	/**
	 * Map from domain response object to ws response object.
	 * 
	 * @param domainResponse
	 *            the JournalfoerInngaaendeHenvendelseResponse
	 *            domain object
	 * @return the ws JournalfoerInngaaendeHenvendelseResponse
	 *         object
	 */
	JournalfoerInngaaendeHenvendelseResponse map(
			no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelseResponse  domainResponse);
}
