package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Value
public class ArkivSakTo {
	@NonNull
	private final String arkivSakId;
	@NonNull
	private final FagsystemCode fagsystem;
}
