package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

import java.math.BigInteger;
import java.sql.Blob;

@Data
@Builder
@AllArgsConstructor
class JoarkDokumentDto {
	private Long journalpostId;
	private String filUuid;
	private String ondemandId;
	private String filtype;
	@ToString.Exclude
	private Blob dokument;

	public JoarkDokumentDto(Object[] objects) {
		this.journalpostId = mapJournalpostId(objects);
		this.filUuid = (String) objects[1];
		this.ondemandId = (String) objects[2];
		this.filtype = (String) objects[3];
		this.dokument = (Blob) objects[4];
	}

	private static Long mapJournalpostId(Object[] objects) {
		if (objects[0] instanceof BigInteger) {
			return ((BigInteger) objects[0]).longValue();
		} else {
			return (Long) objects[0];
		}
	}

	boolean isNormalDocument() {
		return dokument != null && !isDlfDocument();
	}

	boolean isDlfDocument() {
		return dokument != null && FilTypeCode.DLF.name().equals(filtype);
	}

	boolean isOndemandDocument() {
		return dokument == null && ondemandId != null && journalpostId != null && filUuid != null;
	}
}
