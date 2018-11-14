package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.Builder;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@Getter
@Builder
public class SafHentDokumentResponse {
	@Builder.Default
	private final byte[] dokument = new byte[0];
	@Builder.Default
	private final FilTypeCode filtype = null;
}
