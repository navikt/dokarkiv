package no.nav.dokarkiv.journalpost.v1.rjoark202;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.DefaultSporingPopulator;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.rjoark202.util.JournalpostMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(value = "opprettNyJournalpostService")
public class OpprettJournalpostService {

	private final JoarkRepository joarkRepository;
	private final JournalpostMapper journalpostMapper;
	private final DefaultSporingPopulator defaultSporingPopulator;

	@Inject
	public OpprettJournalpostService(final JoarkRepository joarkRepository,
									 final DefaultSporingPopulator defaultSporingPopulator) {
		this.joarkRepository = joarkRepository;
		this.journalpostMapper = new JournalpostMapper();
		this.defaultSporingPopulator = defaultSporingPopulator;
	}

	public Long opprettJournalpost(OpprettJournalpostRequest request) {

		Journalpost journalpost = journalpostMapper.map(request);
		defaultSporingPopulator.populateSporingInfo(journalpost, MDC.get(MDCConstants.MDC_CONSUMER_ID));
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn()));

		// validering ??

		joarkRepository.save(journalpost);

		return journalpost.getJournalpostId();
	}
}
