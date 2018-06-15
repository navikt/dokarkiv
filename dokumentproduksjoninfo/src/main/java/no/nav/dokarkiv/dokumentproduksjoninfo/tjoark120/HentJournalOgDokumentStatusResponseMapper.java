package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;

/**
 * Mapper for HentJournalOgDokumentStatusResponse, maps from domain to WS response.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public interface HentJournalOgDokumentStatusResponseMapper {

	/**
	 * Map from domain to WS response
	 * 
	 * @param domainResponse The domain response
	 * @return The WS response
	 */
	HentJournalOgDokumentStatusResponse map(HentJournalOgDokumentStatusResponseTo domainResponse);
	
}
