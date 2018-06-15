package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusOvergangException;
import no.nav.service.dok.joark.nsb.AvbrytJournalpostValidator;

/**
 * Implementation of the AvbrytJournalpostValidator
 *
 * @author Stig Strøm
 */
public class DefaultAvbrytJournalpostValidator implements AvbrytJournalpostValidator {

	@Override
	public void validate(final Journalpost journalpost) throws UgyldigJournalStatusOvergangException {
		if (journalpost.isInngaende()) {
			throw new UgyldigJournalStatusOvergangException("Kan ikke avbryte en inngående journalpost [journalpostId= "
					+ journalpost.getJournalpostId() + "]", journalpost.getJournalstatus(), JournalStatusCode.A,
					journalpost.getJournalposttype());
		}

		if (journalpost.getJournalstatus() == JournalStatusCode.A) {
			throw new UgyldigJournalStatusOvergangException("Journalpost er allerede avbrutt [journalpostId="
					+ journalpost.getJournalpostId() + "]", journalpost.getJournalstatus(), JournalStatusCode.A,
					journalpost.getJournalposttype());
		} else if (JournalStatusCode.D != journalpost.getJournalstatus() && JournalStatusCode.FL != journalpost.getJournalstatus()) {
			throw new UgyldigJournalStatusOvergangException(
					"JournalStatus er ikke under arbeid eller lokal print, journalposten kan derfor ikke avbrytes [journalpostId="
							+ journalpost.getJournalpostId()
							+ "]", journalpost.getJournalstatus(),
					JournalStatusCode.A, journalpost.getJournalposttype());
		}
	}


}
