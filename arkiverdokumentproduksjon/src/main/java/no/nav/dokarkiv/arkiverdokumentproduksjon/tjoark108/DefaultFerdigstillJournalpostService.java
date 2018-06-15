package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.FilDetaljer;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.domain.dok.joark.codestable.UtsendingsKanalCode;
import no.nav.domain.dok.joark.codestable.VariantFormatCode;
import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.repository.dok.joark.util.DateProvider;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.SporingPopulator;
import no.nav.service.dok.joark.journalbehandling.UgyldigDokumentStatusVerdiException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusVerdiException;
import no.nav.service.dok.joark.nsb.to.FerdigstillJournalpostRequestTo;

import javax.inject.Inject;

/**
 * Implementation of the {@link FerdigstillJournalpostService}
 * 
 * @author Stig Strøm
 *
 */
public class DefaultFerdigstillJournalpostService implements FerdigstillJournalpostService {
	
	@Inject
	private JoarkRepository joarkRepository;
	
	@Inject
	private FerdigstillJournalpostValidator validator;

	@Inject
	private SporingPopulator sporingPopulator;

	@Override
	public void ferdigstillJournalpost(FerdigstillJournalpostRequestTo request) throws NoJournalpostFoundException,
			UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException {
		validator.validateInputRequest(request);		
		Journalpost journalpost = findJournalpost(request.getJournalpostId());
		validator.validate(journalpost);

		UtsendingsKanalCode utsendingskanal = request.getUtsendingskanal();
		if (utsendingskanal == UtsendingsKanalCode.L) {
			journalpost.setJournalstatus(JournalStatusCode.FL);
		} else {
			journalpost.setJournalstatus(JournalStatusCode.FS);
		}
		journalpost.setJournalDato(DateProvider.getToday());
		journalpost.setUtsendingskanal(utsendingskanal);
		journalpost.setJournalfortAvNavn(request.getEndretAvNavn());
		
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			FilDetaljer produksjonFilDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(
					VariantFormatCode.PRODUKSJON);
			if (produksjonFilDetaljer != null) {
				produksjonFilDetaljer.setMetaforceInstanceId(null);
			}
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
