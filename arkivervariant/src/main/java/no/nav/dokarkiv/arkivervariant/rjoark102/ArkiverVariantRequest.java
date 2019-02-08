package no.nav.dokarkiv.arkivervariant.rjoark102;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArkiverVariantRequest {
	private Long dokumentInfoId;
	private VariantFormatCode variant;
	private String fil;
	private FilTypeCode filType;
	private String filnavn;
}
