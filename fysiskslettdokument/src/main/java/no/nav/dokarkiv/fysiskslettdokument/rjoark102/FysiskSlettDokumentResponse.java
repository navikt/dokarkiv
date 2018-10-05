package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;


//IKKE AVKLART HVILKE ELEMENTER SOM SKAL SENDES I RESPONSE

@Data
@Builder
public class FysiskSlettDokumentResponse {

	private final String tittel;
	private final Long dokumentInfoId;
	private final Boolean slettet;
	private final String journalStatus;
	private final Long journalpostId;
	private final String journalpostType;
	private final String tema;

	@JsonCreator
	public FysiskSlettDokumentResponse(@JsonProperty("tittel") String tittel, @JsonProperty("dokumentInfoId") Long dokumentInfoId,
									   @JsonProperty("slettet") Boolean slettet, @JsonProperty("journalStatus") String journalStatus,
									   @JsonProperty("journalpostId") Long journalpostId,
									   @JsonProperty("journalpostType") String journalpostType, @JsonProperty("tema") String tema) {
		this.tittel = tittel;
		this.dokumentInfoId = dokumentInfoId;
		this.slettet = slettet;
		this.journalStatus = journalStatus;
		this.journalpostId = journalpostId;
		this.journalpostType = journalpostType;
		this.tema = tema;
	}
}
