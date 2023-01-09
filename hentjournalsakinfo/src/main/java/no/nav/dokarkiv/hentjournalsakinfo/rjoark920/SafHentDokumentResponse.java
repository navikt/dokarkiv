package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SafHentDokumentResponse {
	@Builder.Default
	private final byte[] dokument = new byte[0];
	@Builder.Default
	private final String filtype = null;
}
