package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class DefaultOpprettJournalpostArkiverDokumenterValidator implements OpprettJournalpostArkiverDokumenterValidator {

	private final JournalpostStructureVerifier verifier;
	private final MandatoryFieldsVerifier mandatoryFieldsVerifier;

	public DefaultOpprettJournalpostArkiverDokumenterValidator(JournalpostStructureVerifier verifier, MandatoryFieldsVerifier mandatoryFieldsVerifier) {
		this.verifier = verifier;
		this.mandatoryFieldsVerifier = mandatoryFieldsVerifier;
	}

	@Override
	public void validate(Journalpost journalpost) {
		verifier.verifyJournalpostStructure(journalpost);
		verifyRequiredFields(journalpost);
		validateJournalpostValues(journalpost);
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(relasjon -> validateCustomDokumentInfoValues(relasjon.getDokumentInfo()));
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

	private void validateJournalpostValues(Journalpost journalpost) {
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
