package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class SafHentJournalpostResponse {

	private final HentJournalpostDto hentJournalpostDto;

	public SafHentJournalpostResponse(@JsonProperty("hentJournalpostDto") HentJournalpostDto hentJournalpostDto) {
		this.hentJournalpostDto = hentJournalpostDto;
	}
}
