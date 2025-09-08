package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.KanIkkeEndreJournalstatusException;

import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static no.nav.dokarkiv.core.util.SafeLoggingUtil.removeUnsafeChars;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class EndreJournalstatusValidator {
	private static final EnumSet<JournalStatusCode> VALID_STATUSES_BEFORE_CHANGE = EnumSet.of(
			M, MO, U, UB
	);

	private EndreJournalstatusValidator() {
	}

	public static void validateEndreJournalstatus(Journalpost journalpost) {
		String feilmeldinger = Stream.concat(
						validateJournalpostInngaaende(journalpost),
						validateJournalstatusBeforeChange(journalpost))
				.collect(Collectors.joining(","));

		if (isNotEmpty(feilmeldinger)) {
			throw new KanIkkeEndreJournalstatusException("Kan ikke endre journalstatus for journalpost: " + feilmeldinger);
		}
	}

	private static Stream<String> validateJournalpostInngaaende(Journalpost journalpost) {
		if (journalpost.getJournalposttype() == JournalpostTypeCode.I) {
			return Stream.empty();
		}
		return Stream.of("Journalpost er ikke av type Inngående (var " + journalpost.getJournalposttype().name() + ")");
	}

	private static Stream<String> validateJournalstatusBeforeChange(Journalpost journalpost) {
		if (VALID_STATUSES_BEFORE_CHANGE.contains(journalpost.getJournalstatus())) {
			return Stream.empty();
		}
		return Stream.of("Journalpost har ikke en av gyldige statuser " + VALID_STATUSES_BEFORE_CHANGE +
				" (var " + journalpost.getJournalposttype().name() + ")");
	}

	public static JournalStatusCode validateAndParseJournalStatus(String journalstatus) {
			return switch (journalstatus) {
				case "UTGAAR" -> U;
				case "UKJENT_BRUKER" -> UB;
				case "MOTTATT" -> M;
				default ->
			throw new InputValideringFeiletException("Ugyldig verdi for Journalstatus: " + removeUnsafeChars(journalstatus));
		};
	}
}
