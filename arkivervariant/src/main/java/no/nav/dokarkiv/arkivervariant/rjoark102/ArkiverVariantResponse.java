package no.nav.dokarkiv.arkivervariant.rjoark102;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@Builder
public class ArkiverVariantResponse {
	private final Long dokumentInfoId;
	private final VariantFormatCode variantFormatCode;
	private final String filUuid;

	@JsonCreator
	public ArkiverVariantResponse(@JsonProperty("dokumentInfoId") Long dokumentInfoId, @JsonProperty("variantFormat") VariantFormatCode variantFormatCode, @JsonProperty("filuuid") String filUuid) {
		this.variantFormatCode = variantFormatCode;
		this.dokumentInfoId = dokumentInfoId;
		this.filUuid = filUuid;
	}
}
