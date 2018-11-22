package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.InngaaendeJournalpostTo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentInngaaendeJournalpostService {

    private final JoarkRepositoryBegrenset repository;
	private final InngaaendeJournalpostToMapper mapper;

	@Inject
    public HentInngaaendeJournalpostService(JoarkRepositoryBegrenset repository, InngaaendeJournalpostToMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	public InngaaendeJournalpostTo hentJournalpost(String journalpostId) {
		try {
			return doHentJournalpost(journalpostId);
		} catch(NumberFormatException e) {
			throw new UgyldigInputException("Tjenesten kan ikke utføres fordi input er ugyldig. journalpostId=" + journalpostId, e);
		}
	}

	private InngaaendeJournalpostTo doHentJournalpost(String journalpostId) {
		assertJournalpostIdIsNotNull(journalpostId);
		Journalpost journalpost = repository.findById(Long.parseLong(journalpostId)).orElse(null);

		if(journalpost == null) {
			throw new JournalpostIkkeFunnetException("Journalpost ikke funnet. journalpostId=" + journalpostId);
		}

		if(!journalpost.isInngaende()){
			throw new JournalpostIkkeInngaaendeException("Journalpost er ikke av type Inngående. journalpostId=" + journalpostId);
		}

		return mapper.map(journalpost);
	}

	public void assertJournalpostIdIsNotNull(String journalpostId) {
		if(journalpostId == null) {
			throw new UgyldigInputException("Tjenesten kan ikke utføres fordi input er ugyldig. journalpostId=null");
		}
	}
}
