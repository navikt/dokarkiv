package no.nav.service.dok.joark.nsb.support;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.FilDetaljer;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.codestable.DokumentStatusCode;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.domain.dok.joark.codestable.UtsendingsKanalCode;
import no.nav.domain.dok.joark.codestable.VariantFormatCode;
import no.nav.repository.dok.joark.JoarkRepository;
import no.nav.repository.dok.joark.util.DateProvider;
import no.nav.service.dok.joark.journalbehandling.DokumentFilerDelegate;
import no.nav.service.dok.joark.journalbehandling.SporingPopulator;
import no.nav.service.dok.joark.nsb.OppdaterJournalpostArkiverDokumentService;
import no.nav.service.dok.joark.nsb.OppdaterJournalpostArkiverDokumentValidator;
import no.nav.service.dok.joark.nsb.exceptions.AlleredeFerdigstiltException;
import no.nav.service.dok.joark.nsb.exceptions.FeilStrukturException;
import no.nav.service.dok.joark.nsb.exceptions.KanIkkeFerdigstillesException;
import no.nav.service.dok.joark.nsb.exceptions.ObjektIkkeFunnetException;
import no.nav.service.dok.joark.nsb.exceptions.UgyldigInputException;
import no.nav.service.dok.joark.nsb.to.OppdaterJournalpostArkiverDokumentRequestTo;

import javax.inject.Inject;
import java.util.Set;

/**
 * Implementation of ArkiverDokumentOgFerdigstillJournalpost
 *
 * @author Torgeir Cook
 */
public class DefaultOppdaterJournalpostArkiverDokumentService implements OppdaterJournalpostArkiverDokumentService {

	@Inject
	private SporingPopulator sporingPopulator;
	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private OppdaterJournalpostArkiverDokumentValidator validator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;

	@Override
	public void oppdaterJournalpostArkiverDokument(OppdaterJournalpostArkiverDokumentRequestTo request) throws UgyldigInputException, ObjektIkkeFunnetException, KanIkkeFerdigstillesException, FeilStrukturException, AlleredeFerdigstiltException {
		validator.validateRequest(request);
		Journalpost journalpost = joarkRepository.findJournalpostByJournalpostId(request.getJournalpostId(), false);

		validator.validate(journalpost, request);
		updateJournalpost(journalpost, request);
		sporingPopulator.populateSporingInfo(journalpost, request.getEndretAvNavn());
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
	}

	public void updateJournalpost(Journalpost journalpost,
								  OppdaterJournalpostArkiverDokumentRequestTo request) {
		UtsendingsKanalCode utsendingskanal = request.getUtsendingskanal();

		if(request.isFerdigstillJournalpost()) {
			if (utsendingskanal == UtsendingsKanalCode.L) {
				journalpost.setJournalstatus(JournalStatusCode.FL);
			} else {
				journalpost.setJournalstatus(JournalStatusCode.FS);
			}
            journalpost.setJournalDato(DateProvider.getToday());
			journalpost.setJournalfortAvNavn(request.getEndretAvNavn());
		}

		journalpost.setUtsendingskanal(utsendingskanal);
        journalpost.setDokumentDato(request.getDatoDokument());
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(request.getDokumentInfoId());
		dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
		dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());

		addUpdateDokumentInfoFilDetaljer(dokumentInfo, request.getFildetaljer(), request.isFerdigstillJournalpost());
	}

	void addUpdateDokumentInfoFilDetaljer(DokumentInfo dokumentInfo, Set<FilDetaljer> filDetaljer,
										  boolean ferdigstillJournalpost) {
		for (FilDetaljer filDetalj : filDetaljer) {
			if (filDetalj.getVariantFormat().equals(VariantFormatCode.PRODUKSJON)) {
				FilDetaljer dokFilDetalj = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON);
				updateFilDetaljWithProduksjonsFormat(dokFilDetalj, filDetalj, ferdigstillJournalpost);
			} else {
				dokumentInfo.addFilDetaljer(filDetalj);
			}
		}
	}

	private void updateFilDetaljWithProduksjonsFormat(FilDetaljer dokFilDetalj, FilDetaljer filDetalj,
													  boolean ferdigstillJournalpost) {
		dokFilDetalj.setFiltype(filDetalj.getFiltype());
		dokFilDetalj.setFileContent(filDetalj.getFileContent());
		dokFilDetalj.setFilstorrelse(filDetalj.getFilstorrelse());
        if(ferdigstillJournalpost) {
            dokFilDetalj.setMetaforceInstanceId(null);
        }
	}
}
