package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@Builder
public class SkjermArkivenhetResponse {
	private Long journalpostId;
	private Long dokumentInfoId;
	private VariantFormatCode variant;
}
