package no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;

import java.util.EnumSet;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;

public class OppdaterJournalpostTypeUtils {

	public static JournalStatusCode determineNewJournalstatusCode(JournalStatusCode currentStatus) {
		return switch (currentStatus) {
			case J -> FL;
			case M, U, MO, UB -> D;
			default ->
					throw new InputValideringFeiletException(format("Ugyldig journalstatus=%s. Gyldige verdier er: %s", currentStatus, EnumSet.of(J, M, U, MO, UB)));
		};
	}

	public static JournalpostTypeCode determineJournalpostTypeCode(JournalpostType journalposttype) {
		return switch (journalposttype) {
			case UTGAAENDE -> JournalpostTypeCode.U;
			case NOTAT -> JournalpostTypeCode.N;
			default ->
					throw new InputValideringFeiletException(format("Ugyldig journalposttype=%s. Gyldige verdier er: %s", journalposttype, EnumSet.of(UTGAAENDE, NOTAT)));
		};
	}

}
