package no.nav.dokarkiv.journal.v3.tjoark050;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;

/**
 * @author Jarl Øystein Samseth, Visma Consulting.
 */
public class HentDokumentURLV3RequestMapper {
	
	public HentDokumentUrlRequestTo map(HentDokumentURLRequest wsRequest) {
		Long journalpostId = Long.parseLong(wsRequest.getJournalpostId());
		Long dokumentInfoId = Long.parseLong(wsRequest.getDokumentId());
		VariantFormatCode variantFormat = VariantFormatCode.valueOf(wsRequest.getVariantformat().getValue());
		return new HentDokumentUrlRequestTo(journalpostId, dokumentInfoId, variantFormat);
	}
}
