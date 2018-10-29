package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArkiverKorrigertDokumentRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
	//TODO: Finn ut type for binærfilen
	private final String binaerFil;

}
