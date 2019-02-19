package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.HentJournalpostDto;

@Value
public class SafHentJournalpostResponseTo {

	private final HentJournalpostDto hentJournalpostDto;

	public SafHentJournalpostResponseTo(@JsonProperty("hentJournalpostDto") HentJournalpostDto hentJournalpostDto) {
		this.hentJournalpostDto = hentJournalpostDto;
	}
}
