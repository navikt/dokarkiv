package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class JournalpostResultResponse {
	private List<JournalpostResponse> oppdatert;
	private List<JournalpostResponse> feilet;
}
