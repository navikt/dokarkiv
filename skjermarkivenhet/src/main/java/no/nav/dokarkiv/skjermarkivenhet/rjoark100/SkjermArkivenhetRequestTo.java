package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@Builder
public class SkjermArkivenhetRequestTo {
	private final ArkivenhetCode arkivenhet;
	private final SkjermingTypeCode skjermingType;
	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final VariantFormatCode variantFormat;
}
