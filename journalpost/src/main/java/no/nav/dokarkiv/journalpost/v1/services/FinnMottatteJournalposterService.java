package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeHenteMottatteJournalposterException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletBruker;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletJournalpost;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static org.slf4j.MDC.get;


@Service
@Slf4j
public class FinnMottatteJournalposterService {

	private final JournalpostRepository journalpostRepository;

	public FinnMottatteJournalposterService(JournalpostRepository journalpostRepository) {
		this.journalpostRepository = journalpostRepository;
	}

	public FinnMottatteJournalposterResponse finnMottatteJournalposter() throws KanIkkeHenteMottatteJournalposterException {
		try {
			List<Journalpost> ubehandledeJournalposter = journalpostRepository
					.findUbehandledeJournalposts(Date.from(LocalDateTime.now().minusWeeks(1).atZone(ZoneId.of("Europe/Oslo")).toInstant()));
			return new FinnMottatteJournalposterResponse(ubehandledeJournalposter.stream().map(this::createResponseObject).collect(Collectors.toList()));
		} catch (DataAccessException e) {
			log.error("{} finnMottatteJournalposter fikk DataAccessException ved kall mot journalpostRepository", get(MDC_REQUEST_ID), e);
			throw new KanIkkeHenteMottatteJournalposterException("Internal server error");
		}
	}

	public FinnMottatteJournalposterResponse finnMottatteJournalposterMedTemaEldreEnn(Set<FagomradeCode> fagomrader, int eldreEnn) throws KanIkkeHenteMottatteJournalposterException {
		try {
			List<Journalpost> ubehandledeJournalposter = journalpostRepository
					.findUbehandledeJournalpostsWithTemaIn(
							Date.from(LocalDateTime.now().minusDays(eldreEnn).atZone(ZoneId.of("Europe/Oslo")).toInstant()),
							fagomrader);
			return new FinnMottatteJournalposterResponse(ubehandledeJournalposter.stream().map(this::createResponseObject).collect(Collectors.toList()));
		} catch (DataAccessException e) {
			log.error("{} finnMottatteJournalposterMedTemaer fikk DataAccessException ved kall mot journalpostRepository.", get(MDC_REQUEST_ID), e);
			throw new KanIkkeHenteMottatteJournalposterException("Internal server error");
		}
	}

	private UbehandletJournalpost createResponseObject(Journalpost journalpost) throws KanIkkeHenteMottatteJournalposterException {

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
			String behandlingstema = journalpost.getBehandlingstema() != null ? journalpost.getBehandlingstema() : null;
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
		} catch (NullPointerException npe) {
			log.error("{} createResponseObject feilet i å generere UbehandletJournalpost objekt for journalpostId: {}", get(MDC_REQUEST_ID), journalpost.getJournalpostId(), npe);
			throw new KanIkkeHenteMottatteJournalposterException("Internal server error");
		} catch (Exception e) {
			log.error("{} createResponseObject, det oppstod en feil. Journalpostid: {}", get(MDC_REQUEST_ID), journalpost.getJournalpostId());
			throw new KanIkkeHenteMottatteJournalposterException("Internal server error");
		}
	}
}
