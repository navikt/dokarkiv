package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Default implementation of SettDatoSendtService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultSettDatoSendtService implements SettDatoSendtService {

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private SporingPopulator sporingPopulator;

	@Override
	public void settDatoSendt(SettDatoSendtRequestTo domainRequest) {
		domainRequest.validate();

		for (Long journalpostId : domainRequest.getJournalpostIds()) {
			Journalpost journalpost = getJournalpost(journalpostId);
			updateJournalpost(domainRequest, journalpost);
		}
	}

	private void updateJournalpost(SettDatoSendtRequestTo domainRequest, Journalpost journalpost) {
		journalpost.setSendtPrintDato(domainRequest.getDatoSendtPrint());
		sporingPopulator.populateSporingInfo(journalpost, domainRequest.getEndretAvNavn());
	}

	private Journalpost getJournalpost(Long journalpostId) {
		return joarkRepository.findById(journalpostId).orElseThrow(() -> new ApplicationException("Could not find Journalpost with journalpostId: " + journalpostId));
	}
}
