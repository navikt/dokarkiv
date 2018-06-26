package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

/**
 * Implementation of the AvbrytJournalpostValidator
 *
 * @author Stig Strøm
 */
@Component
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
