package no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Service
public class HentJournalpostListeService {

	private final JoarkRepository joarkRepository;
	private final JournalpostMapper journalpostMapper;

	@Inject
	public HentJournalpostListeService(JoarkRepository joarkRepository,
									   JournalpostMapper journalpostMapper) {
		this.joarkRepository = joarkRepository;
		this.journalpostMapper = journalpostMapper;
	}

	public HentJournalpostListeResponseTo hentJournalpostListeByArkivIdAndFagsystem(HentJournalpostListeRequestTo hentJournalpostListeRequestTo) {
		return HentJournalpostListeResponseTo.builder()
				.gsakJournalpostList(getJournalpostList(hentJournalpostListeRequestTo.getGsakSakIdList(), FagsystemCode.FS19).stream()
						.map(journalpostMapper::map)
						.collect(Collectors.toList()))
				.psakJournalpostList(getJournalpostList(hentJournalpostListeRequestTo.getPsakSakIdList(), FagsystemCode.PEN).stream()
						.map(journalpostMapper::map)
						.collect(Collectors.toList()))
				.build();
	}

	private List<Journalpost> getJournalpostList(List<String> sakIdList, FagsystemCode fagsystemCode) {
		if (sakIdList == null || sakIdList.isEmpty()) {
			return new ArrayList<>();
		} else {
			return joarkRepository.findJournalposterBySakIdAndFagsystem(sakIdList, fagsystemCode)
					.orElse(new ArrayList<>());
		}
	}

}
