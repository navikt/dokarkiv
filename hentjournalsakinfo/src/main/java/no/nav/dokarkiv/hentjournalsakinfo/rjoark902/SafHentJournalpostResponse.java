package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class SafHentJournalpostResponse {

	HentJournalpostDto hentJournalpostDto;

	public SafHentJournalpostResponse(@JsonProperty("hentJournalpostDto") HentJournalpostDto hentJournalpostDto) {
		this.hentJournalpostDto = hentJournalpostDto;
	}
}
