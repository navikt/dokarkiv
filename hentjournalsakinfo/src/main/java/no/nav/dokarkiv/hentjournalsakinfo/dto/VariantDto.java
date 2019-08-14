package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Value
public class VariantDto {
	private final VariantFormatCode variantf;
	private final String filnavn;
	private final String filuuid;
	private final String filtype;
	private final SkjermingTypeCode skjerming;
}