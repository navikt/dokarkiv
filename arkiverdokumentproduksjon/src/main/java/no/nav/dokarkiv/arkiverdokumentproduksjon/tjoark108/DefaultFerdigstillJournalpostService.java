package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

@Component
public class DefaultFerdigstillJournalpostService implements FerdigstillJournalpostService {

    private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final FerdigstillJournalpostValidator validator;
	private final SporingPopulator sporingPopulator;

	public DefaultFerdigstillJournalpostService(JournalpostRepositorySkjermet journalpostRepositorySkjermet, FerdigstillJournalpostValidator validator, SporingPopulator sporingPopulator) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.validator = validator;
		this.sporingPopulator = sporingPopulator;
	}

	@Override
	public void ferdigstillJournalpost(FerdigstillJournalpostRequestTo request) throws NoJournalpostFoundException,
			UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException {
		validator.validateInputRequest(request);
		Journalpost journalpost = findJournalpost(request.getJournalpostId());
		validator.validate(journalpost);

		UtsendingsKanalCode utsendingskanal = request.getUtsendingskanal();
		if (utsendingskanal == UtsendingsKanalCode.L || utsendingskanal == UtsendingsKanalCode.INGEN_DISTRIBUSJON) {
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
		return journalpostRepositorySkjermet.findById(journalpostId).orElseThrow(() -> new NoJournalpostFoundException("Journalpost with id: " + journalpostId + " does not exist", journalpostId));
	}


}
