package no.nav.dokarkiv.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkjermetVariant {
	private Long dokumentInfoId;
	private VariantFormatCode variantFormat;
}
