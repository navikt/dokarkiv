package no.nav.dokarkiv.journalpost.v1.rjoark202;

import static java.util.Collections.emptyList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;
import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.DefaultSporingPopulator;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.rjoark202.util.JournalpostMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(value = "opprettNyJournalpostService")
public class OpprettJournalpostService {

	public static final String UKJENT = "UKJENT";
	private final JoarkRepository joarkRepository;
	private final JournalpostMapper journalpostMapper;
	private final DefaultSporingPopulator defaultSporingPopulator;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	@Inject
	public OpprettJournalpostService(final JoarkRepository joarkRepository,
									 final DefaultSporingPopulator defaultSporingPopulator,
									 final AksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.journalpostMapper = new JournalpostMapper();
		this.defaultSporingPopulator = defaultSporingPopulator;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	public Long opprettJournalpost(OpprettJournalpostRequest request, String aksjonsLoggHeader) throws UgyldigAksjonsLoggException {

		Journalpost journalpost = journalpostMapper.map(request);
		defaultSporingPopulator.populateSporingInfo(journalpost, MDC.get(MDCConstants.MDC_CONSUMER_ID));
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn()));

		// validering ??

		joarkRepository.save(journalpost);

		populerAksjonslogg(journalpost.getJournalpostId(), aksjonsLoggHeader);

		return journalpost.getJournalpostId();
	}

	private void populerAksjonslogg(Long journalpostId, String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTo;
		if (isBlank(aksjonsLoggHeaderString)) {
			Journalpost journalpost = joarkRepository.findById(journalpostId).orElseThrow(JournalpostIkkeFunnetException::new);
			String bruker = journalpost.getBrukere().isEmpty() ? UKJENT : journalpost.getBrukere().iterator().next().getBrukerId();
			aksjonsLoggTo = AksjonsLoggTO.builder()
					.aksjon(OPPRETT)
					.journalpostId(journalpostId)
					.utfoertAv(MDC.get(MDC_CONSUMER_ID))
					.bruker(bruker)
					.melding("Journalpost opprettet")
					.build();
		} else {
			aksjonsLoggTo = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, OPPRETT, journalpostId, null);
		}
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, emptyList());
	}
}
