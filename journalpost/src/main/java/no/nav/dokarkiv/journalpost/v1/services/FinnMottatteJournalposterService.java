package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.journalpostliste.JournalpostListeRepository;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletBruker;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletJournalpost;
import no.nav.dokarkiv.journalpost.v1.validators.FinnMottateJournalposterValidator;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service(value = "finnMottatteJournalposterService")
@Slf4j
public class FinnMottatteJournalposterService {

	private final JournalpostListeRepository journalpostListeRepository;

	public FinnMottatteJournalposterService(JournalpostListeRepository journalpostListeRepository) {
		this.journalpostListeRepository = journalpostListeRepository;
	}

	public FinnMottatteJournalposterResponse finnMottatteJournalposter(){
		List<Journalpost> ubehandledeJournalposter = journalpostListeRepository.findUbehandletjournalpostListe();
		ubehandledeJournalposter.forEach(FinnMottateJournalposterValidator::validate);
		return new FinnMottatteJournalposterResponse(ubehandledeJournalposter.stream().map(this::createResponseObject).collect(Collectors.toList()));
	}

	private UbehandletJournalpost createResponseObject(Journalpost journalpost){
		UbehandletBruker bruker = journalpost
				.getBrukere()
				.stream()
				.max(Comparator.comparing(o -> o.getChangeStamp().getCreatedDate()))
				.map(e -> new UbehandletBruker(e.getBrukerId(), e.getBrukerType()))
				.orElse(new UbehandletBruker());

		return new UbehandletJournalpost(
				journalpost.getJournalpostId(),
				journalpost.getJournalstatus(),
				journalpost.getMottakskanal(),
				bruker,
				journalpost.getFagomrade(),
				journalpost.getBehandlingstema(),
				journalpost.getJournalForendeEnhetId(),
				journalpost.getChangeStamp().getCreatedDate()
		);
	}
}
