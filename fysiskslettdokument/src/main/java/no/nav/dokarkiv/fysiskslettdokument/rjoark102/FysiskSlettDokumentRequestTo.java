package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;

@Data
@Builder
public class FysiskSlettDokumentRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final BegrensningTypeCode begrensningType;
}
