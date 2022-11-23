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
		return JournalpostResponse.builder()
				.journalpostId(journalpostId)
				.build();
	}

	public static JournalpostResponse error(long journalpostId, String errormessage) {
		return JournalpostResponse.builder()
				.journalpostId(journalpostId)
				.errormessage(errormessage)
				.build();
	}
}
