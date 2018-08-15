package no.nav.dokarkiv.behandlejournal.v3.tjoark064;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseResponse;

/**
 * Mapper for JournalfoerInngaaendeHenvendelseResponse from
 * joark domain to FIM request.
 *
 * @author Rune Romundstad, Visma Consulting
 */
public interface JournalfoerUtgaaendeHenvendelseResponseMapper {

    /**
     * Map from domain response object to ws response object.
     *
     * @param domainResponse
     *            the JournalfoerUtgaaendeHenvendelseResponse
     *            domain object
     * @return the ws JournalfoerUtgaaendeHenvendelseResponse
     *         object
     */
    JournalfoerUtgaaendeHenvendelseResponse map(
			no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseResponse domainResponse);
}
