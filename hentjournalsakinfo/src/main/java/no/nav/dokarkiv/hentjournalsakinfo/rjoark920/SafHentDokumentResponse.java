package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafHentDokumentResponse {
	@Builder.Default
	private final byte[] dokument = null;
	@Builder.Default
	private final FilTypeCode filtype = null;
}
