package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

import no.nav.dokarkiv.behandlejournal.SporingUtil;
import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

@Component
public class DefaultFerdigstillDokumentopplasting implements FerdigstillDokumentopplasting {
	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final SporingPopulator sporingPopulator;

	public DefaultFerdigstillDokumentopplasting(JournalpostRepositorySkjermet journalpostRepositorySkjermet, SporingPopulator sporingPopulator) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.sporingPopulator = sporingPopulator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ferdigstillDokumentOpplasting(FerdigstillDokumentopplastingRequest ferdigstillDokumentOpplastingRequest)
			throws NoJournalpostFoundException {
		validateRequest(ferdigstillDokumentOpplastingRequest);

		handleFerdigstillDokumentOpplasting(ferdigstillDokumentOpplastingRequest.getJournalpostId(),
				ferdigstillDokumentOpplastingRequest.getSporingsMetaData());
	}

	private void handleFerdigstillDokumentOpplasting(Long journalpostId, SporingsMetaData sporingsMetaData)
			throws NoJournalpostFoundException {
		handleJoarkFerdigstillDokumentOpplasting(journalpostId, sporingsMetaData);
	}

	private void handleJoarkFerdigstillDokumentOpplasting(Long journalpostId, SporingsMetaData sporingsMetaData)
			throws NoJournalpostFoundException {
		Journalpost journalpost = journalpostRepositorySkjermet.findById(journalpostId).orElse(null);
		if (journalpost == null) {
			throw new NoJournalpostFoundException("Journalpost with id: " + journalpostId + " does not exist",
					journalpostId);
		}
		validateJournalpost(journalpost);
		updateJournalpost(journalpost, sporingsMetaData);
	}

	private void validateRequest(FerdigstillDokumentopplastingRequest ferdigstillDokumentOpplastingRequest) {
		if (ferdigstillDokumentOpplastingRequest == null) {
			throw new ApplicationException("Missing parameter: ferdigstillJournalpostRequest");
		}
		ferdigstillDokumentOpplastingRequest.validate();
	}

	private void validateJournalpost(Journalpost journalpost) {
		if (journalpost.getJournalposttype() != JournalpostTypeCode.I) {
			throw new ApplicationException("Journalpost is not of type Inngaaende");
		}
		if (journalpost.getJournalstatus() != JournalStatusCode.OD) {
			throw new ApplicationException("Journalpost must have status " + JournalStatusCode.OD);
		}
	}

	private void updateJournalpost(Journalpost journalpost, SporingsMetaData sporingsMetaData) {
		if (FagomradeCode.PEN.equals(journalpost.getFagomrade())) {
			journalpost.setJournalstatus(JournalStatusCode.M);
		} else {
			journalpost.setJournalstatus(JournalStatusCode.MO);
		}
		sporingPopulator.populateSporingInfo(journalpost, SporingUtil.decideSporingNavn(sporingsMetaData));
	}
}
