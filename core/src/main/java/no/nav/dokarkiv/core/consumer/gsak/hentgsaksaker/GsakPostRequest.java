package no.nav.dokarkiv.core.consumer.gsak.hentgsaksaker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
@JsonPropertyOrder({"tema", "applikasjon", "aktoerId", "orgnr", "fagsakNr"})
public class GsakPostRequest {
	@JsonProperty("tema")
	private final String tema;
	@JsonProperty("applikasjon")
	private final String applikasjon;
	@JsonProperty("aktoerId")
	private final String aktoerId;
	@JsonProperty("orgnr")
	private final String orgnr;
	@JsonProperty("fagsakNr")
	private final String fagsakNr;
}
