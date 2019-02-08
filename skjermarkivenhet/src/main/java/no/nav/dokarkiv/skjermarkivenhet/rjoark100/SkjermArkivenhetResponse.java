package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@Builder
public class SkjermArkivenhetResponse {
	private Long journalpostId;
	private Long dokumentInfoId;
	private VariantFormatCode variant;

	@JsonCreator
	public SkjermArkivenhetResponse(@JsonProperty("journalpostId") Long journalpostId, @JsonProperty("dokumentInfoId") Long dokumentInfoId, @JsonProperty("variant") VariantFormatCode variant) {
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
		this.variant = variant;
	}
}
