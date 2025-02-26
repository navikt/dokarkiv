package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeHenteMottatteJournalposterException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static org.slf4j.MDC.get;


@Service
@Slf4j
public class FinnMottatteJournalposterService {

	private final JournalpostRepository journalpostRepository;

	public FinnMottatteJournalposterService(JournalpostRepository journalpostRepository) {
		this.journalpostRepository = journalpostRepository;
	}

	public FinnMottatteJournalposterResponse finnMottatteJournalposterMedTemaEldreEnn(FagomradeCode fagomraade, int eldreEnn, boolean collectBruker) throws KanIkkeHenteMottatteJournalposterException {
		try {
			List<Journalpost> ubehandledeJournalposter = journalpostRepository
					.findUbehandledeJournalpostsForTema(
							LocalDateTime.now().minusDays(eldreEnn),
							fagomraade);
			return new FinnMottatteJournalposterResponse(ubehandledeJournalposter.stream().map(jp -> createResponseObject(jp, collectBruker)).collect(Collectors.toList()));
		} catch (DataAccessException e) {
			throw new KanIkkeHenteMottatteJournalposterException(format("%s finnMottatteJournalposterMedTemaEldreEnn fikk DataAccessException ved kall mot journalpostRepository.", get(MDC_REQUEST_ID)));
		}
	}

	private UbehandletJournalpost createResponseObject(Journalpost journalpost, boolean collectBruker) throws KanIkkeHenteMottatteJournalposterException {
		UbehandletBruker ubehandletBruker = null;
		Date datoOpprettet = journalpost.getChangeStamp() != null ? journalpost.getChangeStamp().getCreatedDate() : null;

		if (collectBruker) {
			ubehandletBruker = journalpost.getBrukere()
					.stream()
					.max(Comparator.comparing(o -> o.getChangeStamp().getCreatedDate()))
					.map(e -> new UbehandletBruker(e.getBrukerId(), e.getBrukerType()))
					.orElse(null);
		}

		return UbehandletJournalpost.builder()
				.journalpostId(journalpost.getJournalpostId())
				.journalStatus(journalpost.getJournalstatus())
				.mottaksKanal(journalpost.getMottakskanal())
				.bruker(ubehandletBruker)
				.tema(journalpost.getFagomrade())
				.behandlingstema(journalpost.getBehandlingstema())
				.journalforendeEnhet(journalpost.getJournalForendeEnhetId())
				.datoOpprettet(datoOpprettet).build();
	}
}
