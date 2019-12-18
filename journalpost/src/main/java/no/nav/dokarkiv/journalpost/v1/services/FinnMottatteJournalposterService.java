package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletBruker;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletJournalpost;
import no.nav.dokarkiv.journalpost.v1.validators.FinnMottatteJournalposterValidator;
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;


@Service
@Slf4j
public class FinnMottatteJournalposterService {

	private final JoarkRepository joarkRepository;

	public FinnMottatteJournalposterService(JoarkRepository joarkRepository) {
		this.joarkRepository = joarkRepository;
	}

	public FinnMottatteJournalposterResponse finnMottatteJournalposter(){
		try {
			List<Journalpost> ubehandledeJournalposter = joarkRepository
					.findUbehandledeJournalposts(DateTime.now().minusWeeks(1).toDate())
					.orElse(List.of());
			ubehandledeJournalposter.forEach(FinnMottatteJournalposterValidator::validate);
			return new FinnMottatteJournalposterResponse(ubehandledeJournalposter.stream().map(this::createResponseObject).collect(Collectors.toList()));
		} catch (DokarkivFunctionalException e) {
			log.error(MDC.get(MDC_REQUEST_ID) + " finnMottatteJournalposter kunne ikke validere ubehandletJournalpost, dette kan tyde på dårlig datakvalitet", e);
			throw e;
		}
	}

	private UbehandletJournalpost createResponseObject(Journalpost journalpost){
		UbehandletBruker bruker = journalpost
				.getBrukere()
				.stream()
				.max(Comparator.comparing(o -> o.getChangeStamp().getCreatedDate()))
				.map(e -> new UbehandletBruker(e.getBrukerId(), e.getBrukerType().name()))
				.orElse(null);

		return new UbehandletJournalpost(
				journalpost.getJournalpostId(),
				journalpost.getJournalstatus().name(),
				journalpost.getMottakskanal().name(),
				bruker,
				journalpost.getFagomrade().name(),
				journalpost.getBehandlingstema().name(),
				journalpost.getJournalForendeEnhetId(),
				journalpost.getChangeStamp().getCreatedDate()
		);
	}
}
