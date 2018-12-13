package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogiskKassasjonResponse {
	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final String tittel;

	@JsonCreator
	public LogiskKassasjonResponse(
			@JsonProperty("journalpostId") Long journalpostId,
			@JsonProperty("dokumentInfoId") Long dokumentInfoId,
			@JsonProperty("tittel") String tittel) {
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
		this.tittel = tittel;
	}
}
