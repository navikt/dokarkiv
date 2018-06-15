package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;

/**
 * Implementation of HentJournalOgDokumentStatusRequestMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class DefaultHentJournalOgDokumentStatusRequestMapper implements HentJournalOgDokumentStatusRequestMapper {

	@Override
	public HentJournalOgDokumentStatusRequestTo map(HentJournalOgDokumentStatusRequest wsRequest) {
		HentJournalOgDokumentStatusRequestTo hentJournalOgDokumentStatusRequestTo = new HentJournalOgDokumentStatusRequestTo();
		hentJournalOgDokumentStatusRequestTo.setJournalpostId(wsRequest.getJournalpostId());
		hentJournalOgDokumentStatusRequestTo.setDokumentInfoId(wsRequest.getDokumentInfoId());
		return hentJournalOgDokumentStatusRequestTo;
	}
}
