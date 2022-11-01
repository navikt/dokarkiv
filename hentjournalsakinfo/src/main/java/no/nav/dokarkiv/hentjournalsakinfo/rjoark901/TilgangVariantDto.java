package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Value
public class TilgangVariantDto {
	VariantFormatCode variantFormat;
	SkjermingTypeCode skjerming;
}