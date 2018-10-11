package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogiskSlettDokumentResponse {

	private final String tittel;
	private final Long dokumentInfoId;
	private final String journalStatus;
	private final Long journalpostId;
	private final String journalpostType;
	private final String tema;

	@JsonCreator
	public LogiskSlettDokumentResponse(@JsonProperty("tittel") String tittel, @JsonProperty("dokumentInfoId") Long dokumentInfoId,
									   @JsonProperty("journalStatus") String journalStatus, @JsonProperty("journalpostId") Long journalpostId,
									   @JsonProperty("journalpostType") String journalpostType, @JsonProperty("tema") String tema) {
		this.tittel = tittel;
		this.dokumentInfoId = dokumentInfoId;
		this.journalStatus = journalStatus;
		this.journalpostId = journalpostId;
		this.journalpostType = journalpostType;
		this.tema = tema;
	}
}
