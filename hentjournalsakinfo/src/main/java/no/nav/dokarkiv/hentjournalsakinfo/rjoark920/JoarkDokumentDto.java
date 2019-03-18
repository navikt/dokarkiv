package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

@Data
@Builder
@AllArgsConstructor
public class JoarkDokumentDto {
	private Long journalpostId;
	private String filUuid;
	private String ondemandId;
	private FilTypeCode filtype;
	private byte[] dokument;

	boolean isNormalDocument() {
		return dokument != null && !isDlfDocument();
	}

	boolean isDlfDocument() {
		return dokument != null && filtype == FilTypeCode.DLF;
	}

	boolean isOndemandDocument() {
		return dokument == null && ondemandId != null && journalpostId != null && filUuid != null;
	}
}
