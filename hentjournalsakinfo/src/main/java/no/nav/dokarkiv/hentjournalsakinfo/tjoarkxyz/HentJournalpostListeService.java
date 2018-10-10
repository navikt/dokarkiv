package no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Service
public class HentJournalpostListeService {

	private final JoarkRepository joarkRepository;

	@Inject
	public HentJournalpostListeService(JoarkRepository joarkRepository) {
		this.joarkRepository = joarkRepository;
	}

	public HentJournalpostListeResponseTo hentJournalpostListeByArkivIdAndFagsystem(HentJournalpostListeRequestTo hentJournalpostListeRequestTo) {
		HentJournalpostListeResponseTo responseTo = new HentJournalpostListeResponseTo();
		responseTo.getGsakJournalpostList()
				.addAll(getJournalpostList(hentJournalpostListeRequestTo.getGsakSakIdList(), FagsystemCode.FS19));
		responseTo.getPsakJournalpostList()
				.addAll(getJournalpostList(hentJournalpostListeRequestTo.getGsakSakIdList(), FagsystemCode.FS19));
		return responseTo;
	}

	private List<Journalpost> getJournalpostList(List<String> gsakSakIdList, FagsystemCode fagsystemCode) {
		List<Journalpost> journalpostList = new ArrayList<>();

		gsakSakIdList.stream().forEach(sakId -> {
			Journalpost journalpost = joarkRepository.findJournalpostIdBySakIdAndFagsystem(sakId, fagsystemCode.name())
					.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med sakId=%s og fagsystem=%s i joark", sakId, fagsystemCode
							.name())));

			journalpostList.add(journalpost);
		});

		return journalpostList;
	}

}
