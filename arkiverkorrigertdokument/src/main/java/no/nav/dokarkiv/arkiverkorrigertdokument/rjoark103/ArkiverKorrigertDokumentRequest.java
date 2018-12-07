package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArkiverKorrigertDokumentRequest {
	private Long dokumentInfoId;
	private String fil;

}
