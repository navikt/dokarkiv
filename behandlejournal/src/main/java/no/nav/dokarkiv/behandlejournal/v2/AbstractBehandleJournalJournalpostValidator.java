package no.nav.dokarkiv.behandlejournal.v2;

import static org.apache.commons.lang.StringUtils.isBlank;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.apache.commons.lang.ArrayUtils;

import javax.inject.Inject;
import java.util.List;

/**
 * Common validation class for Journalpost for the Operations in the
 * BehandleJournal service.
 *
 * @author Rune Romundstad, Visma Consulting
 */
public abstract class AbstractBehandleJournalJournalpostValidator implements BehandleJournalJournalpostValidator {

	@Inject
	protected MandatoryFieldsVerifier mandatoryFieldsVerifier;

	@Inject
	protected JournalpostStructureVerifier journalpostStructureVerifier;

	protected void performCommonValidation(Journalpost journalpost) {
		mandatoryFieldsVerifier.verifyFields(journalpost);
		if (journalpost.getFagomrade() == null) {
			throw new ApplicationException("Fagomrade must be set");
		}

		if (isBlank(journalpost.getJournalForendeEnhetId())) {
			throw new ApplicationException("Field journalfoerendeEnhetId must be set");
		}

		if (journalpost.getInnhold() == null) {
			throw new ApplicationException("Innhold(Tittel) must be set");
		}

		if (journalpost.getOpprettetAvNavn() == null) {
			throw new ApplicationException("OpprettetAvNavn must be set");
		}
		validateSak(journalpost);
	}

	protected void validateMandatoryInngaendeFields(Journalpost journalpost) {

		if (journalpost.getMottattDato() == null) {
			throw new ApplicationException("MottattDato must be set");
		}
		if (journalpost.getDokumentDato() == null) {
			throw new ApplicationException("DokumentDato must be set");
		}
		if (journalpost.getMottakskanal() == null) {
			throw new ApplicationException("Mottakskanal must be set");
		}
		if (journalpost.getSignatur() == null) {
			throw new ApplicationException("Signatur must be set");
		}

		if (journalpost.getOpprettetAvNavn() == null) {
			throw new ApplicationException("OpprettetAvNavn must be set");
		}

		validateHasDokumentInfo(journalpost);
	}

	protected void validateCommonNotatFields(Journalpost journalpost) {
		if (isBlank(journalpost.getJournalForendeEnhetId())) {
			throw new ApplicationException("Field journalfoerendeEnhetId must be set");
		}
		validateHasDokumentInfo(journalpost);
	}

	protected void validateCommonMandatoryUtgaaendeNotatFields(Journalpost journalpost) {
		if (journalpost.getUtsendingskanal() == null) {
			throw new ApplicationException("Utsendingskanal must be set");
		}
		validateHasDokumentInfo(journalpost);
	}

	protected void validateHasDokumentInfo(Journalpost journalpost) {
		if (journalpost.getJournalpostDokumentInfoRelasjoner().isEmpty()
				|| journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo() == null) {
			throw new ApplicationException("Journalpost must have a DokumentInfo");
		}
	}

	protected void validateDokumentInfo(Journalpost journalpost) {
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			validateCommonDokumentInfo(dokumentInfo);
		}
	}

	protected void validateCommonDokumentInfo(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getKategori() == null) {
			throw new ApplicationException("Kategori must be set");
		}
		if (isBlank(dokumentInfo.getTittel())) {
			throw new ApplicationException("Tittel must be set");
		}
	}

	protected void validateBrevkode(DokumentInfo dokumentInfo) {
		if (isBlank(dokumentInfo.getBrevkode())) {
			throw new ApplicationException("Brevkode must be set");
		}
	}

	protected void validateInnskrenketPartsinnsyn(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getInnskrenketPartsinnsyn() == null) {
			throw new ApplicationException("InnskrenketPartsinnsyn must be set");
		}
	}

	protected void validateSensitivt(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getSensitivt() == null) {
			throw new ApplicationException("Sensitivt must be set");
		}
	}

	protected void validateTittel(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getTittel() == null) {
			throw new ApplicationException("Tittel must be set");
		}
	}

	protected void validateKategori(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getKategori() == null) {
			throw new ApplicationException("Kategori must be set");
		}
	}

	protected void validateAvsenderMottaker(Journalpost journalpost) {
		if (isBlank(journalpost.getAvsenderMottakerId())) {
			throw new ApplicationException("AvsenderMottakerId must be set");
		}
		if (isBlank(journalpost.getAvsenderMottaker())) {
			throw new ApplicationException("AvsenderMottaker must be set");
		}
	}

	protected void validateBrukerIsSet(Journalpost journalpost) {
		if (journalpost.getBrukere().isEmpty()) {
			throw new ApplicationException("Journalpost must have a bruker");
		}
	}

	protected void validateCommonNotatDokumentInfo(DokumentInfo dokumentInfo) {
		validateSensitivt(dokumentInfo);

		if (dokumentInfo.getInnskrenketPartsinnsyn() == null) {
			throw new ApplicationException("innskrenketPartsinnsyn must be set");
		}

		if (dokumentInfo.getOrganInternt() == null) {
			throw new ApplicationException("organInternt must be set");
		}
	}

	protected void validateSak(Journalpost journalpost) {
		if (journalpost.getSaksrelasjon() == null) {
			throw new ApplicationException("Missing parameter on journalpost: saksrelasjon");
		}
	}

	protected void validateFileContent(Journalpost journalpost) {
		List<FilDetaljer> filDetaljer = journalpost.findAllFilDetaljer();
		if (filDetaljer == null || filDetaljer.isEmpty()) {
			throw new ApplicationException("FilDetaljer must be set");
		}
		for (FilDetaljer fildetaljer : filDetaljer) {
			if (ArrayUtils.isEmpty(fildetaljer.getFileContent())) {
				throw new ApplicationException("FilDetaljer must have filecontent");
			}			
		}
	}
	
	protected void validateFilDetaljer(Journalpost journalpost) {
		List<FilDetaljer> filDetaljer = journalpost.findAllFilDetaljer();
		if (filDetaljer == null || filDetaljer.isEmpty()) {
			throw new ApplicationException("FilDetaljer must be set");
		}
		for (FilDetaljer fildetaljer : filDetaljer) {			
			if (fildetaljer.getVariantFormat() == null) {
				throw new ApplicationException("FilDetaljer must have variantformat");
			}
			
			if (fildetaljer.getFiltype() == null) {
				throw new ApplicationException("FilDetaljer must have filtype");
			}
		}
		journalpost.verifyNoDokumentVariantDuplicates();
	}

}
