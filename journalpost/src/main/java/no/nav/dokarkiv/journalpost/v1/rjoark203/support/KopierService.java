package no.nav.dokarkiv.journalpost.v1.rjoark203.support;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

public class KopierService {

	public Journalpost copyFrom(Journalpost original) {
		return original.toBuilder()
				.journalstatus(JournalpostTypeCode.I.equals(original.getJournalposttype()) ?
						JournalStatusCode.M : JournalStatusCode.D)
				.build();
	}
}
