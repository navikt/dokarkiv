package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.AlleredeFerdigstiltException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilStrukturException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.KanIkkeFerdigstillesException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ObjektIkkeFunnetException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DefaultOppdaterJournalpostArkiverDokumentService implements OppdaterJournalpostArkiverDokumentService {

	private final SporingPopulator sporingPopulator;
    private final JoarkRepositorySkjermet joarkRepository;
	private final OppdaterJournalpostArkiverDokumentValidator validator;
	private final DokumentFilerDelegate dokumentFilerDelegate;

	public DefaultOppdaterJournalpostArkiverDokumentService(SporingPopulator sporingPopulator, JoarkRepositorySkjermet joarkRepository, OppdaterJournalpostArkiverDokumentValidator validator, DokumentFilerDelegate dokumentFilerDelegate) {
		this.sporingPopulator = sporingPopulator;
		this.joarkRepository = joarkRepository;
		this.validator = validator;
		this.dokumentFilerDelegate = dokumentFilerDelegate;
	}

	@Override
	public void oppdaterJournalpostArkiverDokument(OppdaterJournalpostArkiverDokumentRequestTo request) throws UgyldigInputException, ObjektIkkeFunnetException, KanIkkeFerdigstillesException, FeilStrukturException, AlleredeFerdigstiltException {
		validator.validateRequest(request);
		Journalpost journalpost = joarkRepository.findById(request.getJournalpostId())
				.orElseThrow(() -> new ObjektIkkeFunnetException("JournalpostId eksisterer ikke i Joark", request.getJournalpostId()));

		validator.validate(journalpost, request);
		updateJournalpost(journalpost, request);
		sporingPopulator.populateSporingInfo(journalpost, request.getEndretAvNavn());
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
	}

	public void updateJournalpost(Journalpost journalpost,
								  OppdaterJournalpostArkiverDokumentRequestTo request) {
		UtsendingsKanalCode utsendingskanal = request.getUtsendingskanal();

		if (request.isFerdigstillJournalpost()) {
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
		if (ferdigstillJournalpost) {
			dokFilDetalj.setMetaforceInstanceId(null);
		}
	}
}
