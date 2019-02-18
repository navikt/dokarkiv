package no.nav.dokarkiv.dto;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class SlettArkivenhetRequest {

	private ArkivenhetCode arkivenhet;
	private Long journalpostId;
	private Long dokumentInfoId;
	private VariantFormatCode variant;
}
