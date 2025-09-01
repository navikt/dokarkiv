package no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalposttype;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
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
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class OppdaterJournalpostTypeUtils {

	private static final EnumSet<JournalStatusCode> GODKJENTE_JOURNALSTATUS_FOR_ENDRING = EnumSet.of(M, MO, U, UB, J);

	public static JournalStatusCode determineNewJournalstatusCode(JournalStatusCode currentStatus) {
		return J.equals(currentStatus) ? FL : D;
	}

	public static JournalpostTypeCode determineJournalpostTypeCode(String journalposttype) {
		return UTGAAENDE.name().equals(journalposttype) ? JournalpostTypeCode.U : N;
	}

	public static void validateJournalpostKanEndres(Journalpost journalpostToUpdate) {
		if (journalpostToUpdate.getJournalposttype() != I) {
			throw new InputValideringFeiletException(format("Journalpost med journalpostId=%s har journalposttype=%s og kan derfor ikke endres. Kun journalposter med journalposttype=%s kan endres.",
					journalpostToUpdate.getJournalpostId(),
					journalpostToUpdate.getJournalposttype(),
					JournalpostTypeCode.I));
		}

		if (!GODKJENTE_JOURNALSTATUS_FOR_ENDRING.contains(journalpostToUpdate.getJournalstatus())) {
			throw new InputValideringFeiletException(format("Journalpost med journalpostId=%s har journalstatus=%s og kan derfor ikke endres. Kun journalposter med journalstatus=%s kan endres.",
					journalpostToUpdate.getJournalpostId(),
					journalpostToUpdate.getJournalstatus(),
					GODKJENTE_JOURNALSTATUS_FOR_ENDRING));
		}
	}

	public static void validateOppdaterJournalpostTypeRequest(OppdaterJournalposttypeRequest request) {
		validateJournalfoerendeEnhet(request.journalfoerendeEnhet());
		validateEndresTil(request.typeEndresTil());
	}

	private static void validateJournalfoerendeEnhet(String journalfoerendeEnhet) {
		if (isNotEmpty(journalfoerendeEnhet)) {
			if (journalfoerendeEnhet.length() != 4 || !journalfoerendeEnhet.chars().allMatch(Character::isDigit)) {
				throw new InputValideringFeiletException("Ugyldig journalfoerendeEnhet, må være 4 siffer. Mottok: " + journalfoerendeEnhet);
			}
		}
	}

	private static void validateEndresTil(String typeEndresTil) {
		try {
			JournalpostType endresTil = JournalpostType.valueOf(typeEndresTil);
			if (endresTil != UTGAAENDE && endresTil != NOTAT) {
				throw new InputValideringFeiletException("Ugyldig typeEndresTil, kan kun endres til UTGAAENDE eller NOTAT. Mottok: " + typeEndresTil);
			}
		} catch (Exception e) {
			throw new InputValideringFeiletException("Ugyldig typeEndresTil, kan kun endres til UTGAAENDE eller NOTAT. Mottok: " + typeEndresTil);
		}
	}
}
