package no.nav.dokarkiv.journalpost.v1.services;

import static java.util.Collections.emptyList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.DefaultSporingPopulator;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.mappers.OpprettJournalpostApiRequestMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service(value = "opprettNyJournalpostService")
@Slf4j
public class OpprettJournalpostService {

	public static final String UKJENT = "UKJENT";

	private final JoarkRepository joarkRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final OpprettJournalpostApiRequestMapper opprettJournalpostApiRequestMapper;
	private final DefaultSporingPopulator defaultSporingPopulator;
	private final AksjonsLoggService aksjonsLoggService;

	@Inject
	public OpprettJournalpostService(final JoarkRepository joarkRepository,
									 final DokumentFilRepository dokumentFilRepository,
									 final OpprettJournalpostApiRequestMapper opprettJournalpostApiRequestMapper,
									 final DefaultSporingPopulator defaultSporingPopulator,
									 final AksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.opprettJournalpostApiRequestMapper = opprettJournalpostApiRequestMapper;
		this.defaultSporingPopulator = defaultSporingPopulator;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public Journalpost opprettJournalpost(OpprettJournalpostRequest request) {
		Journalpost journalpost = opprettJournalpostApiRequestMapper.map(request);
		defaultSporingPopulator.populateSporingInfo(journalpost, MDC.get(MDCConstants.MDC_CONSUMER_ID));
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn()));

		persistDokumentFiler(journalpost);

		joarkRepository.save(journalpost);

		populerAksjonslogg(journalpost.getJournalpostId(), OPPRETT);
		log.info(MDC.get(MDC_REQUEST_ID) + " har opprettet ny journalpost, journalpostId={} og status={}", journalpost.getJournalpostId(), journalpost.getJournalstatus());

		return journalpost;
	}

	private void persistDokumentFiler(Journalpost journalpost) {
		List<DokumentFil> dokumentFilList = journalpost.findAllFilDetaljer().stream().map(FilDetaljer::createDokumentFil).collect(Collectors.toList());
		dokumentFilList.forEach(dokumentFilRepository::save);
	}

	private void populerAksjonslogg(Long journalpostId, AksjonsTypeCode aksjon) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElseThrow(JournalpostIkkeFunnetException::new);
		String bruker = journalpost.getBrukere().isEmpty() ? UKJENT : journalpost.getBrukere().iterator().next().getBrukerId();
		AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(journalpostId)
				.utfoertAv(MDC.get(MDC_CONSUMER_ID))
				.bruker(bruker)
				.melding("Journalpost "+aksjon.name())
				.build();
		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, emptyList());
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: "+e.getMessage());
		}
	}
}