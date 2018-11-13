package no.nav.dokarkiv.logiskslettdokument.common;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import org.slf4j.MDC;

//TODO: Skal disse kun brukes i LogiskSlettDokumentService så legger vi de der istedet
public class BegrensningsMetoder {

	public static Begrensning utilgjengeliggjoerHoveddokument(Long journalpostId) {
		Begrensning begrensning = Begrensning.builder()
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.journalpostId(journalpostId)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		return begrensning;
	}

	public static Begrensning utilgjengeliggjoerVedlegg(Long journalpostId, Long dokumentInfoId) {
		Begrensning begrensning = Begrensning.builder()
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
		return begrensning;
	}
}
