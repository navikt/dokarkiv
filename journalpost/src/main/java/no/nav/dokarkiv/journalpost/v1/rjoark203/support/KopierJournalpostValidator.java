package no.nav.dokarkiv.journalpost.v1.rjoark203.support;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeKopiereException;

import java.util.Arrays;
import java.util.List;

public class KopierJournalpostValidator {

	public static final List<JournalStatusCode> COPYABLE_JOURNALSTATUS_LIST = Arrays.asList(FS, FL, E, J);

	public void validate(Journalpost journalpost) {
		JournalStatusCode status = journalpost.getJournalstatus();

		// Verifisere at journalposten er i en tilstand som kan kopieres (status FL, FS, E eller J)
		if (!journalpostHasCopyableStatus(status)) {
			throw new KanIkkeKopiereException(String.format("Kan ikke kopiere journalpost med journalpostId=%s, journalpost har ugyldig status", journalpost.getJournalpostId()));
		}
	}

	private boolean journalpostHasCopyableStatus(JournalStatusCode status) {
		return COPYABLE_JOURNALSTATUS_LIST.contains(status);
	}
}
