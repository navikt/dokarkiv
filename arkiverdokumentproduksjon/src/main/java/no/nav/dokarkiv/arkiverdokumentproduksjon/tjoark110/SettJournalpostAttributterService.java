package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class SettJournalpostAttributterService {

	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final SporingPopulator sporingPopulator;

	public SettJournalpostAttributterService(JournalpostRepositorySkjermet journalpostRepositorySkjermet, SporingPopulator sporingPopulator) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.sporingPopulator = sporingPopulator;
	}

	public void settJournalpostAttributter(SettJournalpostAttributterRequestTo domainRequest) {
		for (Long journalpostId : domainRequest.getJournalpostIds()) {
			Journalpost journalpost = getJournalpost(journalpostId);
			updateJournalpost(domainRequest, journalpost);
		}
	}

	private void updateJournalpost(SettJournalpostAttributterRequestTo domainRequest, Journalpost journalpost) {
		if (domainRequest.getDatoSendtPrint() != null) {
			journalpost.setSendtPrintDato(domainRequest.getDatoSendtPrint());
		}
		if (domainRequest.getUtsendingskanal() != null && domainRequest.getEndretAvNavn() != null) {
			journalpost.setUtsendingskanal(domainRequest.getUtsendingskanal());
		}
		if (!isBlank(domainRequest.getEndretAvNavn())) {
			sporingPopulator.populateSporingInfo(journalpost, domainRequest.getEndretAvNavn());
		}
		if (domainRequest.getAntallRetur() != null) {
			journalpost.setAntallRetur(domainRequest.getAntallRetur());
			journalpost.setAvsendtReturDato(DateProvider.getToday());
		}
	}

	private Journalpost getJournalpost(Long journalpostId) {
		return journalpostRepositorySkjermet.findById(journalpostId)
				.orElseThrow(() -> new ApplicationException("Could not find Journalpost with journalpostId: " + journalpostId));
	}
}
