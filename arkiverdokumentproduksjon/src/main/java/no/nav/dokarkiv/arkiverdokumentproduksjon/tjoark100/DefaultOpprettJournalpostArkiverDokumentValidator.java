package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Validates Journalpost for OpprettOgFerdigstillJournalpost
 *
 * @author Stig Strøm
 */
@Component
public class DefaultOpprettJournalpostArkiverDokumentValidator implements
		OpprettJournalpostArkiverDokumentValidator {

	@Inject
	protected JournalpostStructureVerifier verifier;

	@Inject
	protected MandatoryFieldsVerifier mandatoryFieldsVerifier;

	@Override
	public void validate(Journalpost journalpost, boolean ferdigstillJournalpost) {
		verifier.verifyJournalpostStructure(journalpost);
		verifyRequiredFields(journalpost);
		validateJournalpostValues(journalpost, ferdigstillJournalpost);
		validateCustomDokumentInfoValues(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
	}

	private void verifyRequiredFields(Journalpost journalpost) {
		try {
			mandatoryFieldsVerifier.verifyFields(journalpost);
		} catch (InvalidArgumentException e) {
			if (!contains(e.getMessage(), "journalposttype must be set")) {
				throw new InvalidArgumentException(e.getMessage());
			}
		}
	}

	private void validateJournalpostValues(Journalpost journalpost, boolean ferdigstillJournalpost) {
		if (ferdigstillJournalpost && journalpost.getJournalposttype() == JournalpostTypeCode.U && journalpost.getUtsendingskanal() == null) {
			throw new ApplicationException("Utsendingskanal must be set");
		}

		if (isBlank(journalpost.getJournalForendeEnhetId())) {
			throw new ApplicationException(
					"Field journalfoerendeEnhetId must be set");
		}

		if (journalpost.getDokumentDato() == null) {
			throw new ApplicationException("DatoDokument must be set");
		}
	}

	private void validateCustomDokumentInfoValues(DokumentInfo dokumentInfo) {

		if (dokumentInfo.getSensitivt() == null) {
			throw new ApplicationException("Sensitivt must be set");
		}

		if (isBlank(dokumentInfo.getBrevkode())) {
			throw new ApplicationException("Brevkode must be set");
		}

		if (isBlank(dokumentInfo.getTittel())) {
			throw new ApplicationException("Tittel must be set");
		}

		if (isBlank(dokumentInfo.getDokumenttypeId())) {
			throw new ApplicationException("DokumenttypeId must be set");
		}

		if (dokumentInfo.getKategori() == null) {
			throw new ApplicationException("Kategori must be set");
		}

		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			validateFilDetaljer(filDetaljer);
		}
	}

	private void validateFilDetaljer(FilDetaljer filDetaljer) {
		filDetaljer.verifyMandatoryFields();
		if (filDetaljer.getFileContent() == null) {
			throw new ApplicationException("FileContent must be set");
		}
	}

}
