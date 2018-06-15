package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;

/**
 * Mapper for HentJournalOgDokumentStatusRequest, maps from WS to domain request.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public interface HentJournalOgDokumentStatusRequestMapper {

    /**
     * Map from WS request to domain request.
     *
     * @param wsRequest The WS request
     * @return The domain request
     */
    HentJournalOgDokumentStatusRequestTo map(HentJournalOgDokumentStatusRequest wsRequest);

}
