package no.nav.dokarkiv.fysisktidligkassasjon.rjoark107;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FysiskTidligKassasjonResponse {
	private final Long dokumentInfoId;
	private final String tittel;

	@JsonCreator
	public FysiskTidligKassasjonResponse(
			@JsonProperty("dokumentInfoId") Long dokumentInfoId,
			@JsonProperty("tittel") String tittel) {
		this.dokumentInfoId = dokumentInfoId;
		this.tittel = tittel;
	}

}
