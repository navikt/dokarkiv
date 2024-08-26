package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import org.springframework.stereotype.Service;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.BESTILLINGS_ID_KEY;
import static org.assertj.core.util.Strings.isNullOrEmpty;

@Service
public class DefaultOpprettJournalpostArkiverDokumentService implements OpprettJournalpostArkiverDokumentService {

    private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final OpprettJournalpostArkiverDokumentValidator opprettJournalpostArkiverDokumentValidator;
	private final DokumentFilerDelegate dokumentFilerDelegate;

	public DefaultOpprettJournalpostArkiverDokumentService(JournalpostRepositorySkjermet journalpostRepositorySkjermet, OpprettJournalpostArkiverDokumentValidator opprettJournalpostArkiverDokumentValidator, DokumentFilerDelegate dokumentFilerDelegate) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.opprettJournalpostArkiverDokumentValidator = opprettJournalpostArkiverDokumentValidator;
		this.dokumentFilerDelegate = dokumentFilerDelegate;
	}

	@Override
	public OpprettJournalpostArkiverDokumentResponseTo opprettJournalpostArkiverDokument(
			OpprettJournalpostArkiverDokumentRequestTo requestTo) {
		this.validateRequest(requestTo);
		Journalpost storedJournalpost = findPreviousJournalforing(requestTo);
		if (storedJournalpost == null) {
			Journalpost journalpost = requestTo.getJournalpost();

			updateJournalpost(journalpost, requestTo.isFerdigstillJournalpost());

			opprettJournalpostArkiverDokumentValidator.validate(journalpost, requestTo.isFerdigstillJournalpost());

			dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
			storedJournalpost = journalpostRepositorySkjermet.save(journalpost);
		}
		return createResponse(storedJournalpost);
	}

	private void validateRequest(OpprettJournalpostArkiverDokumentRequestTo request) {
		if (request == null) {
			throw new ApplicationException("Missing parameter: request");
		}
	}

	private OpprettJournalpostArkiverDokumentResponseTo createResponse(Journalpost journalpost) {
		return new OpprettJournalpostArkiverDokumentResponseTo(
				journalpost.getJournalpostId(),
				journalpost.findHoveddokumentDokumentInfoRelasjon().
						getDokumentInfo().getDokumentInfoId());
	}


	private void updateJournalpost(Journalpost journalpost, boolean ferdigstillJournalpost) {
		if (ferdigstillJournalpost) {
			if (journalpost.getUtsendingskanal() == UtsendingsKanalCode.L) {
				journalpost.setJournalstatus(JournalStatusCode.FL);
			} else {
				journalpost.setJournalstatus(JournalStatusCode.FS);
			}
			journalpost.setJournalDato(DateProvider.getToday());
			journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());
			journalpost.setUtsendingskanal(journalpost.getUtsendingskanal());
		} else {
			journalpost.setJournalstatus(JournalStatusCode.D);
			journalpost.setJournalDato(null);
			journalpost.setJournalfortAvNavn(null);
			journalpost.setUtsendingskanal(null);
		}
		if (journalpost.getJournalposttype() == null) {
			journalpost.setJournalposttype(JournalpostTypeCode.U);
		}

		JournalpostDokumentInfoRelasjon relasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		relasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
		dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());
		dokumentInfo.setOriginalJournalpost(journalpost);
	}

	private Journalpost findPreviousJournalforing(OpprettJournalpostArkiverDokumentRequestTo requestTo) {
		final DokumentInfo dokumentInfo = requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		final String bestillingsId = dokumentInfo.getTilleggsopplysninger().get(BESTILLINGS_ID_KEY);
		if (isNullOrEmpty(bestillingsId)) {
			return null;
		}

		Long journalpostIdPreviousJournalforing = findPreviousJournalpostIdByDokumentInfoTilleggsopplysningerBestillingsId(bestillingsId);
		if (journalpostIdPreviousJournalforing == null) {
			return null;
		} else {
			return journalpostRepositorySkjermet.findById(journalpostIdPreviousJournalforing).orElse(null);
		}
	}

	private Long findPreviousJournalpostIdByDokumentInfoTilleggsopplysningerBestillingsId(final String bestillingsId) {
		Long dokumentinfoIdPreviousJournalforing = journalpostRepositorySkjermet.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(BESTILLINGS_ID_KEY, bestillingsId);
		if (dokumentinfoIdPreviousJournalforing == null) {
			return null;
		}

		return journalpostRepositorySkjermet.findJournalpostIdByDokumentinfoId(dokumentinfoIdPreviousJournalforing);
	}
}
