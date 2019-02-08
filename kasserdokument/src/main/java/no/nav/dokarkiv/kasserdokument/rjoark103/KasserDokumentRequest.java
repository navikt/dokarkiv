package no.nav.dokarkiv.kasserdokument.rjoark103;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KasserDokumentRequest {
	private Long dokumentInfoId;
	private String kassertAvNavn;
}
