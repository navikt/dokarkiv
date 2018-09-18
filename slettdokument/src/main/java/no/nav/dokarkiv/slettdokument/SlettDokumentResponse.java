package no.nav.dokarkiv.slettdokument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlettDokumentResponse {

	private String tittel;
	private Long dokumentInfoId;
	private String journalStatus;
	private Long journalpostId;
	private String journalpostType;
	private String tema;
}
