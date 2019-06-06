package no.nav.dokarkiv.core.aksjonslogg;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class JournalpostDokumentInfoPair {
	private Long journalpostId;
	private Long dokumentInfoId;

	public static JournalpostDokumentInfoPair of(final Long journalpostId, final Long dokumentInfoId) {
		return JournalpostDokumentInfoPair.builder()
				.dokumentInfoId(dokumentInfoId)
				.journalpostId(journalpostId)
				.build();
	}


}
