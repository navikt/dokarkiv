package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;

@Data
@Builder
public class FysiskSlettDokumentResponse {

	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final String tittel;
	private final TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode;

	@JsonCreator
	public FysiskSlettDokumentResponse(
			@JsonProperty("journalpostId") Long journalpostId,
			@JsonProperty("dokumentInfoId") Long dokumentInfoId,
			@JsonProperty("tittel") String tittel,
			@JsonProperty("tittel") TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode) {
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
		this.tittel = tittel;
		this.tilknyttetJournalpostSomCode = tilknyttetJournalpostSomCode;
	}
}
