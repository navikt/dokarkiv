package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.service.dok.joark.journalbehandling.SporingPopulator;
import no.nav.service.dok.joark.nsb.to.SettDatoSendtRequestTo;

import javax.inject.Inject;

/**
 * Default implementation of SettDatoSendtService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultSettDatoSendtService implements SettDatoSendtService {

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private SporingPopulator sporingPopulator;

	@Override
	public void settDatoSendt(SettDatoSendtRequestTo domainRequest) {
		domainRequest.validate();

		for(Long journalpostId : domainRequest.getJournalpostIds()) {
			Journalpost journalpost = getJournalpost(journalpostId);
			updateJournalpost(domainRequest, journalpost);
		}
	}

	private void updateJournalpost(SettDatoSendtRequestTo domainRequest, Journalpost journalpost) {
		journalpost.setSendtPrintDato(domainRequest.getDatoSendtPrint());
		sporingPopulator.populateSporingInfo(journalpost, domainRequest.getEndretAvNavn());
	}

	private Journalpost getJournalpost(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findJournalpostById(journalpostId);
		if(journalpost == null) {
			throw new ApplicationException("Could not find Journalpost with journalpostId: " + journalpostId);
		}
		return journalpost;
	}
}
