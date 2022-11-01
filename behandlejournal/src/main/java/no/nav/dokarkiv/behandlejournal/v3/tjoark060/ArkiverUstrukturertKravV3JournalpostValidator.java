package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalV3JournalpostValidator;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ArkiverUstrukturertKravV3JournalpostValidator extends AbstractBehandleJournalV3JournalpostValidator {

	public ArkiverUstrukturertKravV3JournalpostValidator(MandatoryFieldsVerifier mandatoryFieldsVerifier, JournalpostStructureVerifier journalpostStructureVerifier) {
		super(mandatoryFieldsVerifier, journalpostStructureVerifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(Journalpost journalpost) {
		mandatoryFieldsVerifier.verifyFields(journalpost);
		if (journalpost.getFagomrade() == null) {
			throw new ApplicationException("Fagomrade must be set");
		}
		validateMandatoryInngaendeFields(journalpost);
		validateBrukerIsSet(journalpost);
		validateFileContent(journalpost);
		journalpost.verifyNoDokumentVariantDuplicates();
		validateMottakerId(journalpost);
	}


	private void validateMottakerId(Journalpost journalpost) {
		if (journalpost.getAvsenderMottaker() != null || !StringUtils.isBlank(journalpost.getAvsenderMottakerId())) {
			if (StringUtils.isBlank(journalpost.getAvsenderMottakerId())) {
				throw new ApplicationException("Journalpost.AvsenderMottakerId must be set when " +
						"Journalpost.AvsenderMottaker is set");
			}
			if (journalpost.getAvsenderMottaker() == null) {
				throw new ApplicationException("Journalpost.AvsenderMottaker must be set when " +
						"Journalpost.AvsenderMottakerId is set");
			}
		}

		/*
		 Hack innført i PK-25537. Bør tas bort i HL4-2015.
		 Journalpost.Eksternpart.navn som mappes til Journalpost.avsenderMottaker er påkrevd i grensesnittet fra KES, men
		 konsumenten har ikke mulighet til å sende inn denne informasjonen i HL3-2015. Derfor tillates det pr HL3 at konsument
		 sender inn tom streng. Dette bør endres i HL4-2015, slik av det kastes exception dersom avsenderMottakerId er satt
		 og avsenderMottaker er tom. Det betyr at if-clausen under bør fjernes, og != null sjekkene i valideringen bør byttes
		 ut med isBlank sjekker.
		 */
		if ("".equals(journalpost.getAvsenderMottaker())) {
			journalpost.setAvsenderMottaker(null);
		}
	}

}
