package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Value
public class AktoerTo {
	@NonNull
	private final String aktoerId;
	@NonNull
	private final BrukerTypeCode aktoerType;
}
