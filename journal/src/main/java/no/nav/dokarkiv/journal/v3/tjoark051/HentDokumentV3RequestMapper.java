package no.nav.dokarkiv.journal.v3.tjoark051;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest;
import org.springframework.stereotype.Component;


@Component
public class HentDokumentV3RequestMapper {

	/**
	 * maps from wsRequest to domainRequest
	 *
	 * @param wsRequest the request from ws
	 * @return {@link HentDokumentRequestTo} the domainRequest
	 */
	public HentDokumentRequestTo map(HentDokumentRequest wsRequest) {
		Long journalpostId = Long.parseLong(wsRequest.getJournalpostId());
		Long dokumentInfoId = Long.parseLong(wsRequest.getDokumentId());
		VariantFormatCode variantFormat = VariantFormatCode.valueOf(wsRequest.getVariantformat().getValue());
		return new HentDokumentRequestTo(journalpostId, dokumentInfoId, variantFormat);
	}
}
