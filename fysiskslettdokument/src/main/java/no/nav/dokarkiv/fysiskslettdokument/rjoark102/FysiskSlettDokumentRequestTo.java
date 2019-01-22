package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;

@Data
@Builder
public class FysiskSlettDokumentRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final SkjermingTypeCode begrensningType;
}
