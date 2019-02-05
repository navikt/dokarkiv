package no.nav.dokarkiv.skjermarkivenhet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkjermArkivenhetHeader {
	private SkjermingTypeCode skjerming;
	private ArkivenhetCode arkivenhet;
	private Long journalpostId;
	private Long dokumentInfoId;
	private VariantFormatCode variant;
}
