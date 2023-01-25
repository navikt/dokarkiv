package no.nav.dokarkiv.behandlejournal.v3.tjoark064;


import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalV3JournalpostValidator;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class JournalfoerUtgaaendeHenvendelseV3Validator extends AbstractBehandleJournalV3JournalpostValidator {

	public JournalfoerUtgaaendeHenvendelseV3Validator(MandatoryFieldsVerifier mandatoryFieldsVerifier, JournalpostStructureVerifier journalpostStructureVerifier) {
		super(mandatoryFieldsVerifier, journalpostStructureVerifier);
	}

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
		validateSensitivt(dokumentInfo);
	}
}
