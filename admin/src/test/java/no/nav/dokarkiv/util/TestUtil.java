package no.nav.dokarkiv.util;

import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import no.nav.dokarkiv.dto.SkjermArkivenhetRequest;

public class TestUtil {

	public static String KASSERT_AV_NAVN = "Kåre Kassasjon";

	public static SkjermArkivenhetRequest createSkjermarkivenhetRequest(SkjermingTypeCode skjermingType, ArkivenhetCode arkivenhet, Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {
		return SkjermArkivenhetRequest.builder()
				.skjerming(skjermingType)
				.arkivenhet(arkivenhet)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.variant(variantFormat)
				.build();
	}

	public static KasserDokumentRequest createKasserDokumentRequest(Long dokumentInfoId) {
		return KasserDokumentRequest.builder().dokumentInfoId(dokumentInfoId).kassertAvNavn(KASSERT_AV_NAVN).build();
	}

}

