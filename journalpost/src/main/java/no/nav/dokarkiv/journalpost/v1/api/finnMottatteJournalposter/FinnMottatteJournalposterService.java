package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.exceptions.KanIkkeHenteMottatteJournalposterException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.projections.MottattBrukerProjection;
import no.nav.dokarkiv.core.repository.projections.MottattJournalpostProjection;
import no.nav.dokarkiv.core.repository.projections.MottattJournalpostProjectionMedBruker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FinnMottatteJournalposterService {

	private final JournalpostRepository journalpostRepository;

	public FinnMottatteJournalposterService(JournalpostRepository journalpostRepository) {
		this.journalpostRepository = journalpostRepository;
	}

	public FinnMottatteJournalposterResponse finnMottatteJournalposterUtenBrukerMedTemaEldreEnn(FagomradeCode fagomraade, int eldreEnn) throws KanIkkeHenteMottatteJournalposterException {
		Set<MottattJournalpostProjection> mottatteJournalposter = journalpostRepository
				.finnMottatteJournalposterForTemaUtenBruker(
						LocalDateTime.now().minusDays(eldreEnn),
						fagomraade);

		return new FinnMottatteJournalposterResponse(mottatteJournalposter.stream().map(this::mapUbehandletJournalpost).collect(Collectors.toSet()));
	}

	public FinnMottatteJournalposterResponse finnMottatteJournalposterMedBrukerMedTemaEldreEnn(FagomradeCode fagomraade, int eldreEnn) throws KanIkkeHenteMottatteJournalposterException {
		Set<MottattJournalpostProjectionMedBruker> mottatteJournalposter = journalpostRepository
				.finnMottatteJournalposterForTemaMedBruker(
						LocalDateTime.now().minusDays(eldreEnn),
						fagomraade);
		return new FinnMottatteJournalposterResponse(mottatteJournalposter.stream().map(this::mapUbehandletJournalpostMedBruker).collect(Collectors.toSet()));

	}

	private MottattJournalpost mapUbehandletJournalpostMedBruker(MottattJournalpostProjectionMedBruker journalpostMedBruker) {
		MottattJournalpost.MottattJournalpostBuilder builder = mapUbehandletJournalpostBuilder(journalpostMedBruker);
		MottattJournalpostBruker mottattJournalpostBruker = journalpostMedBruker.getBrukere()
				.stream()
				.max(Comparator.comparing(MottattBrukerProjection::getDatoOpprettet))
				.map(e -> new MottattJournalpostBruker(e.getBrukerId(), e.getBrukerType()))
				.orElse(null);

		return builder.bruker(mottattJournalpostBruker).build();
	}

	private MottattJournalpost mapUbehandletJournalpost(MottattJournalpostProjection journalpost) {
		return mapUbehandletJournalpostBuilder(journalpost).build();
	}

	private MottattJournalpost.MottattJournalpostBuilder mapUbehandletJournalpostBuilder(MottattJournalpostProjection journalpost) {
		return MottattJournalpost.builder()
				.journalpostId(journalpost.getJournalpostId())
				.journalStatus(journalpost.getJournalstatus())
				.mottaksKanal(journalpost.getMottakskanal())
				.tema(journalpost.getFagomrade())
				.behandlingstema(journalpost.getBehandlingstema())
				.journalforendeEnhet(journalpost.getJournalForendeEnhetId())
				.datoOpprettet(journalpost.getDatoOpprettet().atZone(ZONEID_NORGE).toOffsetDateTime());
	}

}
