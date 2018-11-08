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
	private final Long journalpostId;
	private final Boolean slettet;

	@JsonCreator
	public LogiskSlettDokumentResponse(
			@JsonProperty("tittel") String tittel,
			@JsonProperty("dokumentInfoId") Long dokumentInfoId,
			@JsonProperty("journalpostId") Long journalpostId,
			@JsonProperty("slettet") Boolean slettet) {
		this.tittel = tittel;
		this.dokumentInfoId = dokumentInfoId;
		this.journalpostId = journalpostId;
		this.slettet = slettet;
	}
}
