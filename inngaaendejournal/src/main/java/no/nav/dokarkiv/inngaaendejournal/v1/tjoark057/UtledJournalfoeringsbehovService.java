package no.nav.dokarkiv.inngaaendejournal.v1.tjoark057;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalpostManglerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.exceptions.JournalpostKanIkkeBehandlesException;
import no.nav.dokarkiv.inngaaendejournal.v1.exceptions.UgyldigInputException;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.JournalpostManglerToMapper;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class UtledJournalfoeringsbehovService {

	private final JoarkRepository repository;
	private final JournalpostManglerToMapper mapper;

	@Inject
	public UtledJournalfoeringsbehovService(JoarkRepository repository, JournalpostManglerToMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}


	public JournalpostManglerTo utledJournalfoeringsbehov(String journalpostId) {
		try {
			return doUtledJournalfoeringsbehov(journalpostId);
		} catch (NumberFormatException e) {
			throw new UgyldigInputException("Tjenesten kan ikke utføres fordi input er ugyldig. journalpostId=" + journalpostId, e);
		}
	}

	private JournalpostManglerTo doUtledJournalfoeringsbehov(String journalpostId) {
		if (journalpostId == null) {
			throw new UgyldigInputException("Tjenesten kan ikke utføres fordi input er ugyldig. journalpostId=null");
		}

		Journalpost journalpost = repository.findById(Long.parseLong(journalpostId)).orElse(null);

		if (journalpost == null) {
			throw new JournalpostIkkeFunnetException("Journalpost ikke funnet. journalpostId=" + journalpostId);
		}

		validateJournalpost(journalpost);

		return mapper.map(journalpost);
	}

	private void validateJournalpost(Journalpost journalpost) {
		if (!journalpost.hasMidlertidigInngaaendeJournalforingStatus()) {
			throw new JournalpostKanIkkeBehandlesException("Journalpost må ha midlertidig status. journalpostId=" +
					journalpost.getJournalpostId() + ", journalStatus=" + journalpost.getJournalstatus());
		}

		if (journalpost.isFeilregistrert()) {
			throw new JournalpostKanIkkeBehandlesException("Journalpost kan ikke være feilregistrert. journalpostId=" + journalpost.getJournalpostId());
		}

		if (!journalpost.isInngaende()) {
			throw new JournalpostIkkeInngaaendeException("Journalpost er ikke av type Inngående. journalpostId=" + journalpost.getJournalpostId());
		}
	}

}
