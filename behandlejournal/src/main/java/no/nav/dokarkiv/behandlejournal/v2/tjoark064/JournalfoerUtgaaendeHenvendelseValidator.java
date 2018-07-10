package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

import static org.apache.commons.lang.StringUtils.isBlank;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalJournalpostValidator;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * Validator for the JournalfoerUtgaaendeHenvendelseMedHoveddokument service
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerUtgaaendeHenvendelseValidator extends
		AbstractBehandleJournalJournalpostValidator {

	@Override
	public void validate(Journalpost journalpost) {
		performCommonValidation(journalpost);
		validateBrukerIsSet(journalpost);
		validateCommonMandatoryUtgaaendeNotatFields(journalpost);
		validateCustomJournalpostValues(journalpost);
		validateCustomDokumentInfoValues(journalpost);
		validateFileContent(journalpost);
		validateFilDetaljer(journalpost);
	}

	private void validateCustomJournalpostValues(Journalpost journalpost) {
		validateAvsenderMottaker(journalpost);
		if (isBlank(journalpost.getJournalForendeEnhetId())) {
			throw new ApplicationException("JournalforendeEnhetId must be set");
		}
	}

	private void validateCustomDokumentInfoValues(Journalpost journalpost) {
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();
		validateBrevkode(dokumentInfo);
		validateInnskrenketPartsinnsyn(dokumentInfo);
		validateSensitivt(dokumentInfo);
	}
}
