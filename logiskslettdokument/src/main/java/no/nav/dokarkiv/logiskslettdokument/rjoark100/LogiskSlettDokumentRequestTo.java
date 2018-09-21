package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import lombok.Builder;
import lombok.Data;

/**
 * RequestTo object for SlettDokument
 */
@Data
@Builder
public class LogiskSlettDokumentRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
}
