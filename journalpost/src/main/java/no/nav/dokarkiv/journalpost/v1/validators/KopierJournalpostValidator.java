package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeKopiereException;

import java.util.EnumSet;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;

public class KopierJournalpostValidator {

	private static final EnumSet<JournalStatusCode> GYLDIGE_JOURNALSTATUSER_FOR_KOPIERING = EnumSet.of(FS, FL, E, J);

	public void validate(Journalpost journalpost) {
		JournalStatusCode status = journalpost.getJournalstatus();

		// Verifisere at journalposten er i en tilstand som kan kopieres (status FL, FS, E eller J)
		if (!GYLDIGE_JOURNALSTATUSER_FOR_KOPIERING.contains(status)) {
			throw new KanIkkeKopiereException(format("Kan ikke kopiere journalpost med kildeJournalpostId=%s fordi journalpost har ugyldig status=%s", journalpost.getJournalpostId(), journalpost.getJournalstatus()));
		}
	}

}