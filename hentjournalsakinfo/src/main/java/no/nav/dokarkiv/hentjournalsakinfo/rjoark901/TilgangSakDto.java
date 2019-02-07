package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
@AllArgsConstructor
public class TilgangSakDto {

	private final String sakId;
	private final FagsystemCode fagsystem;

}
