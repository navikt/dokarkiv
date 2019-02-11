package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
public class TilgangBrukerDto {

	String brukerId;
	BrukerTypeCode brukerType;
}
