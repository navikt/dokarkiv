package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafHentDokumentResponse {
	@Builder.Default
	private final Base64 dokument = null;
	@Builder.Default
	private final String filtype = null;
}
