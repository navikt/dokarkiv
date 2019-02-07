package no.nav.dokarkiv.arkivervariant.rjoark102;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArkiverVariantRequest {
	private Long dokumentInfoId;
	private String variant;
	private String fil;
	private String filType;
	private String filnavn;
}
