package no.nav.dokarkiv.safintern.finnjournalposter;

import lombok.ToString;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.List;

public record FinnJournalposterRequest(
		List<Long> gsakSakIds,
		List<Long> psakSakIds,
		String fraDato,
		String tilDato,
		Boolean visFeilregistrerte,
		@ToString.Exclude
		List<String> alleIdenter,
		List<JournalStatusCode> journalstatuser,
		List<JournalpostTypeCode> journalposttyper,
		Integer antallRader,
		String etterPeker
) {
}
