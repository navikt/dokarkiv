package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class SaksrelasjonDto {
	private String sakId;
	private Boolean feilregistrert;
	private FagsystemCode fagsystem;
}
