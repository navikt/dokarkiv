package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

/**
 * Mapper for JournalfoerUtgaaendeHenvendelseRequest from FIM (MOD) to domain (JOARK)
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
public interface JournalfoerUtgaaendeHenvendelseRequestMapper {
	/**
	 * Map from ws request to domain request.
	 * 
	 * @param wsRequest The ws request
	 * @return The domain request
	 */
	JournalfoerUtgaaendeHenvendelseRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerUtgaaendeHenvendelseRequest wsRequest);
}
