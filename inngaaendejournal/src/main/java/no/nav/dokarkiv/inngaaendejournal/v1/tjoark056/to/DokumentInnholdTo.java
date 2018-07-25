package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Value
public class DokumentInnholdTo {
	@NonNull
	private final FilTypeCode arkivFiltype;
	@NonNull
	private final VariantFormatCode variantFormat;
}
