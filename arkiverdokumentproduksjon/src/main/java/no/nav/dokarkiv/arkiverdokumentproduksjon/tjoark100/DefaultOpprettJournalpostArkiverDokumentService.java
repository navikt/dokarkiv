package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import static no.nav.dokarkiv.core.constants.ServiceConstants.BESTILLINGS_ID_KEY;
import static no.nav.service.dok.joark.ServiceConstants.BESTILLINGS_ID_KEY;

import com.google.common.base.Strings;
import no.nav.dokarkiv.core.domain.DokumentInfo;
import no.nav.dokarkiv.core.domain.Journalpost;
import no.nav.dokarkiv.core.domain.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.util.DateProvider;
import no.nav.modig.core.exception.ApplicationException;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;

/**
 * Implementation of OpprettJournalpostArkiverDokumentService
 *
 * @author Cook, Torgeir
 */
public class DefaultOpprettJournalpostArkiverDokumentService implements OpprettJournalpostArkiverDokumentService {

	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private OpprettJournalpostArkiverDokumentValidator opprettJournalpostArkiverDokumentValidator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;

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
			storedJournalpost = joarkRepository.save(journalpost);
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
		if (CollectionUtils.isEmpty(dokumentInfo.getTilleggsopplysninger())) {
			return null;
		}

		String bestillingsId = dokumentInfo.getTilleggsopplysninger().get(BESTILLINGS_ID_KEY);
		if (Strings.isNullOrEmpty(bestillingsId)) {
			return null;
		}

		Long findJournalpostTilleggssopplysning = joarkRepository.findJournalpostWithDokumentInfoTilleggsopplysning(BESTILLINGS_ID_KEY, bestillingsId);

		return joarkRepository.findById(findJournalpostTilleggssopplysning);
	}
}
