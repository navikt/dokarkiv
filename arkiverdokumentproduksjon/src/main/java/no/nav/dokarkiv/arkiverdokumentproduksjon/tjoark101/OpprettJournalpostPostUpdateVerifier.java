package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;


import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

/**
 * Validates journalpost fields after update of jouralpost.
 *
 * @author Hans Petter Simonsen - Visma Consulting AS
 */
@Component
public class OpprettJournalpostPostUpdateVerifier implements OpprettJournalpostValidator {
	@Override
	public void validate(Journalpost journalpost) {
		validateJournalStatus(journalpost, JournalStatusCode.D);
		validateJournalpostType(journalpost, JournalpostTypeCode.U);
		validateJournalpostDokumentInfoRelasjonTilknyttetSom(journalpost, TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		validateDokumentStatus(journalpost, DokumentStatusCode.UNDER_REDIGERING);
	}


	private void validateDokumentStatus(Journalpost journalpost, DokumentStatusCode dokumentStatusCode) {
		Iterable<DokumentInfo> dokumentInfos = journalpost.findDokumentInfoByDokumentStatus(dokumentStatusCode);
		if (!dokumentInfos.iterator().hasNext()) {
			throw new ApplicationException(
					"Expected dokumentInfo with dokumentStatusCode "
							+ dokumentStatusCode + ". Not found.");
		}
	}

	private void validateJournalpostDokumentInfoRelasjonTilknyttetSom(
			Journalpost journalpost,
			TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode) {
		if (journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(tilknyttetJournalpostSomCode) == null) {
			throw new ApplicationException(
					"Expected dokumentInfoRelasjon tilknyttetSom "
							+ tilknyttetJournalpostSomCode + ". Not found");
		}
	}

	private void validateJournalpostType(Journalpost journalpost, JournalpostTypeCode journalpostTypeCode) {
		if (!journalpostTypeCode.equals(journalpost.getJournalposttype())) {
			throw new ApplicationException("Expected journalpostType "
					+ journalpostTypeCode + ", got "
					+ journalpost.getJournalposttype());
		}
	}

	private void validateJournalStatus(Journalpost journalpost, JournalStatusCode journalStatusCode) {
		if (!journalStatusCode.equals(journalpost.getJournalstatus())) {
			throw new ApplicationException("Expected journalstatus "
					+ journalStatusCode + ", got "
					+ journalpost.getJournalstatus());
		}
	}

}
