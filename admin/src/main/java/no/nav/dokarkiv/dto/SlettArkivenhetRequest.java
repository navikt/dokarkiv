package no.nav.dokarkiv.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@Builder
@AllArgsConstructor
public class SlettArkivenhetRequest {

	private ArkivenhetCode arkivenhet;
	private Long journalpostId;
	private Long dokumentInfoId;
	private VariantFormatCode variant;
}
