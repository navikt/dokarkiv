package no.nav.dokarkiv.innsynjournal.v2.tjoark054;

import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

/**
 * Domain request for HentDokument(TJOARK051 and TJOARK054)
 *
 * @author Stig Strøm
 */
@Data
public class HentDokumentRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final VariantFormatCode variantFormat;
}
