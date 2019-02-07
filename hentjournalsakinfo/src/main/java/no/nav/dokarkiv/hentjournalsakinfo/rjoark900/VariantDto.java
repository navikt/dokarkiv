package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Value
public class VariantDto {
	private final VariantFormatCode variantf;
	private final SkjermingTypeCode skjerming;
}