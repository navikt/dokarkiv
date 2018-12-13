package no.nav.dokarkiv.tidligkassasjon.rjoark106;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TidligKassasjonResponse {
	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final String tittel;

	@JsonCreator
	public TidligKassasjonResponse(
			@JsonProperty("journalpostId") Long journalpostId,
			@JsonProperty("dokumentInfoId") Long dokumentInfoId,
			@JsonProperty("tittel") String tittel) {
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
		this.tittel = tittel;
	}

}
