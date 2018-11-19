package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

@Data
@Builder
@AllArgsConstructor
public class SafHentDokumentDto {
	private byte[] dokument;
	private FilTypeCode variantFormat;
}
