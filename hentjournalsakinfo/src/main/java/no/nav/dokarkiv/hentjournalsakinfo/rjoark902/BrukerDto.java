package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class BrukerDto {
	private final String brukerId;
	private final String brukerIdType;
}
