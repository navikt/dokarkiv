package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FysiskSlettDokumentRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
}
