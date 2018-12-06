package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArkiverKorrigertDokumentRequest {
	private Long dokumentInfoId;
	private VariantFormatCode variantFormatCode;
	private String fil;

}
