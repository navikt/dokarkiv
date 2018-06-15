package no.nav.service.dok.joark.nsb.support;

import javax.inject.Inject;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.FilDetaljer;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.codestable.DokumentStatusCode;
import no.nav.domain.dok.joark.codestable.VariantFormatCode;
import no.nav.repository.dok.joark.DokumentFilRepository;
import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.NoDokumentInfoFoundException;
import no.nav.service.dok.joark.journalbehandling.SporingPopulator;
import no.nav.service.dok.joark.journalbehandling.UgyldigDokumentStatusVerdiException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusVerdiException;
import no.nav.service.dok.joark.nsb.FjernFerdigstiltDokumentService;
import no.nav.service.dok.joark.nsb.FjernFerdigstiltDokumentValidator;
import no.nav.service.dok.joark.nsb.to.FjernFerdigstiltDokumentRequestTo;

/**
 * Implementation of the {@link FjernFerdigstiltDokumentService}
 * 
 * @author Stig Strøm
 *
 */
public class DefaultFjernFerdigstiltDokumentService implements FjernFerdigstiltDokumentService {
	
	
	
	@Inject
	private JoarkRepository joarkRepository;
	
	@Inject 
	DokumentFilRepository dokumentFilRepository;
	
	@Inject
	private FjernFerdigstiltDokumentValidator fjernFerdigstiltDokumentValidator;

	@Inject
	private SporingPopulator sporingPopulator;


	@Override
	public void fjernFerdigstiltDokument(FjernFerdigstiltDokumentRequestTo request) throws NoJournalpostFoundException,
			NoDokumentInfoFoundException, UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException {
		fjernFerdigstiltDokumentValidator.validateInputRequest(request);
		
		Journalpost journalpost = findJournalpost(request.getJournalpostId());
		fjernFerdigstiltDokumentValidator.validate(journalpost, request);
		
		journalpost.setDokumentDato(null);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(request.getDokumentInfoId());
		dokumentInfo.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		dokumentInfo.setDokumentFerdigDato(null);
		
		
		FilDetaljer arkivFilDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		if (arkivFilDetaljer != null) {
			dokumentFilRepository.deleteDokumentFil(arkivFilDetaljer.getFilUuid());
			dokumentInfo.removeFilDetaljer(arkivFilDetaljer);
		}
		sporingPopulator.populateSporingInfo(journalpost, request.getEndretAvNavn());
	}
	
	
	
	private Journalpost findJournalpost(Long journalpostId) throws NoJournalpostFoundException {
		Journalpost journalpost = joarkRepository.findJournalpostById(journalpostId);
		if (journalpost == null) {
			throw new NoJournalpostFoundException("Journalpost with id: " + journalpostId + " does not exist", journalpostId);
		}
		return journalpost;
	}


	

}
