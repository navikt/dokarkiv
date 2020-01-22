package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeHenteMottatteJournalposterException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletBruker;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletJournalpost;
import org.joda.time.DateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static org.slf4j.MDC.get;


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
			return new FinnMottatteJournalposterResponse(ubehandledeJournalposter.stream().map(this::createResponseObject).collect(Collectors.toList()));
		} catch(DataAccessException e){
			log.error(get(MDC_REQUEST_ID) + " finnMottatteJournalposter fikk DataAccessException ved kall mot joarkRepository", e);
			throw new KanIkkeHenteMottatteJournalposterException("Internal server error");
		}
	}

	public FinnMottatteJournalposterResponse finnMottatteJournalposterMedTema(List<String> temaer){
		try {
			List<Journalpost> ubehandledeJournalposter = joarkRepository
					.finnMottatteJournalposterFoerDato(DateTime.now().minusWeeks(1).toDate(), temaer)
					.orElse(List.of());
			return new FinnMottatteJournalposterResponse(ubehandledeJournalposter.stream().map(this::createResponseObject).collect(Collectors.toList()));
		} catch(DataAccessException e){
			log.error(get(MDC_REQUEST_ID) + " finnMottatteJournalposterMedTemaer fikk DataAccessException ved kall mot joarkRepository", e);
			throw new KanIkkeHenteMottatteJournalposterException("Internal server error");
		}
	}
	private UbehandletJournalpost createResponseObject(Journalpost journalpost){

		try {
			long journalpostId = journalpost.getJournalpostId();
			String journalStatus = journalpost.getJournalstatus() != null ? journalpost.getJournalstatus().name() : null;
			String mottaksKanal = journalpost.getMottakskanal() != null ? journalpost.getMottakskanal().name() : null;
			UbehandletBruker bruker = journalpost
					.getBrukere()
					.stream()
					.max(Comparator.comparing(o -> o.getChangeStamp().getCreatedDate()))
					.map(e -> new UbehandletBruker(e.getBrukerId(), e.getBrukerType().name()))
					.orElse(null);
			String tema = journalpost.getFagomrade() != null ? journalpost.getFagomrade().name() : null;
			String behandlingstema = journalpost.getBehandlingstema() != null ? journalpost.getBehandlingstema().name() : null;
			String journalforendeEnhet = journalpost.getJournalForendeEnhetId();
			Date datoOpprettet = journalpost.getChangeStamp() != null ? journalpost.getChangeStamp().getCreatedDate() : null;

			return new UbehandletJournalpost(
					journalpostId,
					journalStatus,
					mottaksKanal,
					bruker,
					tema,
					behandlingstema,
					journalforendeEnhet,
					datoOpprettet
			);
		} catch(NullPointerException npe) {
			log.error(get(MDC_REQUEST_ID) + " createResponseObject feilet i å generere UbehandletJournalpost objekt for journalpostId: " + journalpost.getJournalpostId(), npe);
			throw new KanIkkeHenteMottatteJournalposterException("Internal server error");
		} catch(Exception e) {
			log.error(get(MDC_REQUEST_ID) + " createResponseObject, det oppstod en feil. Journalpostid: " + journalpost.getJournalpostId());
			throw new KanIkkeHenteMottatteJournalposterException("Internal server error");
		}
	}
}
