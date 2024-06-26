package no.nav.dokarkiv.safintern.journalstatus;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.List;

public record FinnJournalposterStatusRequest(
		JournalStatusCode journalstatus,
		String fraDato,
		List<JournalpostTypeCode> journalposttyper,
		Integer foerste,
		Long etterPeker
) { }
