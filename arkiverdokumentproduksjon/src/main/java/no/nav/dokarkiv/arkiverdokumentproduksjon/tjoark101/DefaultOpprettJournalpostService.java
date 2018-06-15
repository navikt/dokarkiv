package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import javax.inject.Inject;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.domain.dok.joark.codestable.DokumentStatusCode;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.domain.dok.joark.codestable.JournalpostTypeCode;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.repository.dok.joark.util.DateProvider;
import no.nav.service.dok.joark.journalbehandling.DokumentFilerDelegate;
import no.nav.service.dok.joark.nsb.OpprettJournalpostService;
import no.nav.service.dok.joark.nsb.OpprettJournalpostValidator;
import no.nav.service.dok.joark.nsb.to.OpprettJournalpostRequestTo;
import no.nav.service.dok.joark.nsb.to.OpprettJournalpostResponseTo;

/**
 * Implementation of the OpprettJournalpostService
 * 
 * @author Stig Strøm
 */
public class DefaultOpprettJournalpostService implements OpprettJournalpostService {

	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private OpprettJournalpostValidator opprettJournalpostValidator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;

	@Override
	public OpprettJournalpostResponseTo opprettJournalpost(
			OpprettJournalpostRequestTo opprettJournalpostRequest) {
		validateRequest(opprettJournalpostRequest);
		
		Journalpost journalpost = opprettJournalpostRequest.getJournalpost();
		updateJournalpost(journalpost);
		opprettJournalpostValidator.validate(journalpost);
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		Journalpost storedJournalpost = joarkRepository.saveNewJournalPost(journalpost);
		return createResponse(storedJournalpost);
	}
	
	private void validateRequest(OpprettJournalpostRequestTo request) {
		if (request == null) {
			throw new ApplicationException("Missing parameter: request");
		}
		request.validate();
	}
	
	private OpprettJournalpostResponseTo createResponse(Journalpost journalpost) {
		OpprettJournalpostResponseTo response = new OpprettJournalpostResponseTo(
				journalpost.getJournalpostId(),
				journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId());
		return response;
	}
	
	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		journalpost.setJournalstatus(JournalStatusCode.D);
		journalpost.setJournalDato(DateProvider.getToday());
		journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());	
		JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hoveddokumentDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = hoveddokumentDokumentInfoRelasjon.getDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());
		dokumentInfo.setOriginalJournalpost(journalpost);
	}
}
