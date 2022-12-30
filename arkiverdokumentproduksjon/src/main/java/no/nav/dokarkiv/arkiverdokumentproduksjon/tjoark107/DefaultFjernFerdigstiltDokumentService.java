package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

@Component
public class DefaultFjernFerdigstiltDokumentService implements FjernFerdigstiltDokumentService {

	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final DokumentFilRepository dokumentFilRepository;
	private final FjernFerdigstiltDokumentValidator fjernFerdigstiltDokumentValidator;
	private final SporingPopulator sporingPopulator;

	public DefaultFjernFerdigstiltDokumentService(JournalpostRepositorySkjermet journalpostRepositorySkjermet, DokumentFilRepository dokumentFilRepository, FjernFerdigstiltDokumentValidator fjernFerdigstiltDokumentValidator, SporingPopulator sporingPopulator) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.dokumentFilRepository = dokumentFilRepository;
		this.fjernFerdigstiltDokumentValidator = fjernFerdigstiltDokumentValidator;
		this.sporingPopulator = sporingPopulator;
	}

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

		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			if (filDetaljer.getVariantFormat().equals(VariantFormatCode.ARKIV) || filDetaljer.getVariantFormat().equals(VariantFormatCode.SLADDET)) {
				dokumentFilRepository.deleteByFilUuid(filDetaljer.getFilUuid());
				dokumentInfo.removeFilDetaljer(filDetaljer);
			}
		}
		sporingPopulator.populateSporingInfo(journalpost, request.getEndretAvNavn());
	}

	private Journalpost findJournalpost(Long journalpostId) throws NoJournalpostFoundException {
		return journalpostRepositorySkjermet.findById(journalpostId).orElseThrow(() -> new NoJournalpostFoundException("Journalpost with id: " + journalpostId + " does not exist", journalpostId));
	}


}
