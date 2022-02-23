package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Validator class for JournalforInngaaendeForsendelseV2 (TJOARK203)
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class JournalforInngaaendeForsendelseV2Validator {

	@Inject
	private MandatoryFieldsVerifier mandatoryFieldsVerifier;

	public void validate(final Journalpost journalpost) {
		mandatoryFieldsVerifier.verifyFieldsSkipJournalForendeEnhetId(journalpost);
		validateFagomrade(journalpost);
		validateJournalpost(journalpost);
	}

	private void validateFagomrade(Journalpost journalpost) {
		// Vi gjør dette for at journalstatus skal bli satt til M hvis tema er ukjent.
		if(journalpost.getFagomrade() == FagomradeCode.UKJ) {
			throw new IllegalArgumentException("Tema was missing in request. This has been mapped to tema UKJ.");
		}
	}

	public void validateVariantFormaterAndHoveddokument(Journalpost journalpost) {
		journalpost.verifyArkivVariantOfAllDocuments();
		journalpost.verifyNoDokumentVariantDuplicates();
		journalpost.verifyOnlyOneHoveddokument();
	}

	private void validateJournalpost(Journalpost journalpost) {
		notNull(journalpost.getDokumentDato(), "Missing required field in request: Journalpost.DokumentDato");
		notNull(journalpost.getMottattDato(), "Missing required field in request: Journalpost.MottatDato");
		notNull(journalpost.getMottakskanal(), "Missing required field in request: Journalpost.Mottakskanal");
	}
}
