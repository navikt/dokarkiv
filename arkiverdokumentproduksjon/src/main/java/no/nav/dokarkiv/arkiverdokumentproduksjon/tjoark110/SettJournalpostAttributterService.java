package no.nav.service.dok.joark.arkiverdokumentproduksjon;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.service.dok.joark.journalbehandling.SporingPopulator;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SettJournalpostAttributterService {
	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private SporingPopulator sporingPopulator;

	public void settJournalpostAttributter(SettJournalpostAttributterRequestTo domainRequest) {
		for(Long journalpostId : domainRequest.getJournalpostIds()) {
			Journalpost journalpost = getJournalpost(journalpostId);
			updateJournalpost(domainRequest, journalpost);
		}
	}

	private void updateJournalpost(SettJournalpostAttributterRequestTo domainRequest, Journalpost journalpost) {
		if(domainRequest.getDatoSendtPrint() != null) {
			journalpost.setSendtPrintDato(domainRequest.getDatoSendtPrint());
		}
		if(!isBlank(domainRequest.getEndretAvNavn())) {
			sporingPopulator.populateSporingInfo(journalpost, domainRequest.getEndretAvNavn());
		}
		if(domainRequest.getAntallRetur() != null) {
			journalpost.setAntallRetur(domainRequest.getAntallRetur());
		}
	}

	private Journalpost getJournalpost(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findJournalpostById(journalpostId);
		if(journalpost == null) {
			throw new ApplicationException("Could not find Journalpost with journalpostId: " + journalpostId);
		}
		return journalpost;
	}
}
