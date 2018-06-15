package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import static no.nav.service.dok.joark.ServiceConstants.BESTILLINGS_ID_KEY;

import com.google.common.base.Strings;
import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.domain.dok.joark.codestable.DokumentStatusCode;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.domain.dok.joark.codestable.JournalpostTypeCode;
import no.nav.domain.dok.joark.codestable.UtsendingsKanalCode;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.repository.dok.joark.util.DateProvider;
import no.nav.service.dok.joark.journalbehandling.DokumentFilerDelegate;
import no.nav.service.dok.joark.nsb.OpprettJournalpostArkiverDokumentValidator;
import no.nav.service.dok.joark.nsb.to.OpprettJournalpostArkiverDokumentRequestTo;
import no.nav.service.dok.joark.nsb.to.OpprettJournalpostArkiverDokumentResponseTo;
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
		if(storedJournalpost == null) {
			Journalpost journalpost = requestTo.getJournalpost();

			updateJournalpost(journalpost, requestTo.isFerdigstillJournalpost());

			opprettJournalpostArkiverDokumentValidator.validate(journalpost, requestTo.isFerdigstillJournalpost());

			dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
			storedJournalpost = joarkRepository.saveNewJournalPost(journalpost);
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
		return joarkRepository.findJournalpostById(findJournalpostTilleggssopplysning);
	}
}
