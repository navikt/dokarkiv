package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class JournalpostResponse {
	private Long journalpostId;
	private String errormessage;

	public static JournalpostResponse ok(long journalpostId) {
		return new JournalpostResponse(journalpostId, null);
	}

	public static JournalpostResponse error(Long journalpostId, String errormessage) {
		return new JournalpostResponse(journalpostId, errormessage);
	}
}
