package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;

@Value
public class TilgangBrukerDto {

	String brukerId;
	BrukerTypeCode brukerType;
}
