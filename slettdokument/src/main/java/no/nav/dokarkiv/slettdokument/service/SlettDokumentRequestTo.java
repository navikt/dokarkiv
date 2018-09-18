package no.nav.dokarkiv.slettdokument.service;

import lombok.Builder;
import lombok.Data;

/**
 * RequestTo object for SlettDokument
 */
@Data
@Builder
public class SlettDokumentRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
}
