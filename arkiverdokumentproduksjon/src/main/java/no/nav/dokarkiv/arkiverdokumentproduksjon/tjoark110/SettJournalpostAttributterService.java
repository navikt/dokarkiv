package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SettJournalpostAttributterService {
	@Inject
	private JoarkRepositorySkjermet joarkRepository;

	@Inject
	private SporingPopulator sporingPopulator;

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
		if (!isBlank(domainRequest.getEndretAvNavn())) {
			sporingPopulator.populateSporingInfo(journalpost, domainRequest.getEndretAvNavn());
		}
		if (domainRequest.getAntallRetur() != null) {
			journalpost.setAntallRetur(domainRequest.getAntallRetur());
		}
		if (domainRequest.getUtsendingskanal() != null) {
			journalpost.setUtsendingskanal(domainRequest.getUtsendingskanal());
		}
	}

	private Journalpost getJournalpost(Long journalpostId) {
		return joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new ApplicationException("Could not find Journalpost with journalpostId: " + journalpostId));
	}
}
