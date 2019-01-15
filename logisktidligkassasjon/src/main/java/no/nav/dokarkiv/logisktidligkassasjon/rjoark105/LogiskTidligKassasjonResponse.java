package no.nav.dokarkiv.logisktidligkassasjon.rjoark105;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogiskTidligKassasjonResponse {
	private final Long dokumentInfoId;
	private final String tittel;

	@JsonCreator
	public LogiskTidligKassasjonResponse(
			@JsonProperty("dokumentInfoId") Long dokumentInfoId,
			@JsonProperty("tittel") String tittel) {
		this.dokumentInfoId = dokumentInfoId;
		this.tittel = tittel;
	}
}
