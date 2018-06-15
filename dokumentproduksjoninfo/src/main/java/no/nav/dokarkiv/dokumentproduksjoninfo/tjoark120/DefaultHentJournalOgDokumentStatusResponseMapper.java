package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;

/**
 * Implementation of HentJournalOgDokumentStatusResponseMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class DefaultHentJournalOgDokumentStatusResponseMapper implements HentJournalOgDokumentStatusResponseMapper {

	@Override
	public HentJournalOgDokumentStatusResponse map(HentJournalOgDokumentStatusResponseTo domainResponse) {
		HentJournalOgDokumentStatusResponse hentJournalOgDokumentStatusResponse = new HentJournalOgDokumentStatusResponse();
		hentJournalOgDokumentStatusResponse.setDokumentStatus(domainResponse.getDokumentStatus().name());
		hentJournalOgDokumentStatusResponse.setJournalStatus(domainResponse.getJournalStatus().name());
		hentJournalOgDokumentStatusResponse.setMetaForceInstanceId(domainResponse.getMetaforceInstanceId());
		return hentJournalOgDokumentStatusResponse;
	}
}
