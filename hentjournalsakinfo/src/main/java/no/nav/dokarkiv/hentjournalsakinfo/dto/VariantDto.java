package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Value
public class VariantDto {
	VariantFormatCode variantf;
	String filnavn;
	String filuuid;
	String filtype;
	String filstorrelse;
	SkjermingTypeCode skjerming;
}