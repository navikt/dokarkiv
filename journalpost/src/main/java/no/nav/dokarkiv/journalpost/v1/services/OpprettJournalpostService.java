package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import no.nav.dokarkiv.core.sporing.DefaultSporingPopulator;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResult;
import no.nav.dokarkiv.journalpost.v1.mappers.OpprettJournalpostApiRequestMapper;
import no.nav.dokarkiv.journalpost.v1.util.opprettjournalpost.OpprettJournalpostPDFAUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.MIGRERING_L;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.MIGRERING_S;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service(value = "opprettNyJournalpostService")
@Slf4j
public class OpprettJournalpostService {

	public static final String UKJENT = "UKJENT";
	private static final String APPLIKASJON_FS22 = "FS22";
	private static final EnumSet<UtsendingsKanalCode> UTGAAENDE_UTSENDING_IDEMPOTENT_REFERANSE_ID =
			EnumSet.of(MIGRERING_S, MIGRERING_L);

	private final JoarkRepository joarkRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final OpprettJournalpostApiRequestMapper opprettJournalpostApiRequestMapper;
	private final DefaultSporingPopulator defaultSporingPopulator;
	private final AksjonsLoggService aksjonsLoggService;
	private final IdentConsumer identConsumer;
	private final HentSakerRepository hentSakerRepository;
	private final OpprettJournalpostPDFAUtils opprettJournalpostPDFAUtils;

	@Inject
	public OpprettJournalpostService(final JoarkRepository joarkRepository,
									 final DokumentFilRepository dokumentFilRepository,
									 final OpprettJournalpostApiRequestMapper opprettJournalpostApiRequestMapper,
									 final DefaultSporingPopulator defaultSporingPopulator,
									 final AksjonsLoggService aksjonsLoggService,
									 final IdentConsumer identConsumer,
									 final HentSakerRepository hentSakerRepository,
									 final OpprettJournalpostPDFAUtils opprettJournalpostPDFAUtils) {
		this.joarkRepository = joarkRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.opprettJournalpostApiRequestMapper = opprettJournalpostApiRequestMapper;
		this.defaultSporingPopulator = defaultSporingPopulator;
		this.aksjonsLoggService = aksjonsLoggService;
		this.identConsumer = identConsumer;
		this.hentSakerRepository = hentSakerRepository;
		this.opprettJournalpostPDFAUtils = opprettJournalpostPDFAUtils;
	}

	public OpprettJournalpostResult opprettJournalpost(OpprettJournalpostRequest request) {

		Optional<Journalpost> existingJournalpost = findJournalpostWithIdempodentKanalAlreadyInDb(request);
		if (existingJournalpost.isPresent()) {
			final Journalpost journalpost = existingJournalpost.get();
			log.warn("Journalpost med eksternReferanseId={} for kanal={} finnes fra før. Oppretter ikke ny journalpost.", request.getEksternReferanseId(), journalpost.getMottakskanal());
			return new OpprettJournalpostResult(journalpost, false);
		}
		String sakId = hentSakId(request);

		Journalpost journalpost = opprettJournalpostApiRequestMapper.map(request, sakId);
		defaultSporingPopulator.populateSporingInfo(journalpost, MDC.get(MDCConstants.MDC_USER_NAME));
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn()));

		persistDokumentFiler(journalpost);

		joarkRepository.save(journalpost);

		populerAksjonslogg(journalpost.getJournalpostId(), OPPRETT);
		log.info(MDC.get(MDC_REQUEST_ID) + " har opprettet ny journalpost, journalpostId={} og status={}", journalpost.getJournalpostId(), journalpost.getJournalstatus());

		opprettJournalpostPDFAUtils.validateAndLogPDFA(journalpost);

		return new OpprettJournalpostResult(journalpost, true);
	}

	private String hentSakId(OpprettJournalpostRequest request) {
		if (request.getSak() != null) {
			Sakstype sakstype = request.getSak().getSakstype();
			if ((FAGSAK.equals(sakstype) || Sakstype.GENERELL_SAK.equals(sakstype)) && !Fagsaksystem.PP01.equals(request.getSak().getFagsaksystem())) {
				return identifiserEllerOpprettArkivsak(request);
			}
		}
		return null;
	}

	private String identifiserEllerOpprettArkivsak(OpprettJournalpostRequest request) {
		Sak sak = createSak(request);
		List<Sak> saker = hentSakerRepository.finnSaker(SakSearchCriteria.builder()
				.aktoerId(sak.getAktoerId())
				.orgnr(sak.getOrgnr())
				.tema(Collections.singletonList(sak.getTema()))
				.applikasjon(sak.getApplikasjon())
				.fagsakNr(sak.getFagsakNr())
				.build());
		if (saker.isEmpty()) {
			return hentSakerRepository.lagre(sak).getSakId().toString();
		} else {
			return saker.stream().map(Sak::getSakId).max(Comparator.naturalOrder()).orElseThrow(UgyldigInputException::new).toString();
		}
	}

	private Sak createSak(OpprettJournalpostRequest request) {
		return Sak.builder()
				.aktoerId(hentAktoerId(request.getBruker()))
				.orgnr(BrukerIdType.ORGNR.equals(request.getBruker().getIdType()) ?
						request.getBruker().getId() : null)
				.tema(request.getTema())
				.applikasjon(FAGSAK.equals(request.getSak().getSakstype()) ?
						request.getSak().getFagsaksystem().name() : APPLIKASJON_FS22)
				.fagsakNr(FAGSAK.equals(request.getSak().getSakstype()) ?
						request.getSak().getFagsakId() : null)
				.opprettetAv(MDC.get(MDC_CONSUMER_ID))
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
	}

	private String hentAktoerId(Bruker bruker) {
		switch (bruker.getIdType()) {
			case AKTOERID:
				return bruker.getId();
			case FNR:
				return identConsumer.hentAktoerId(bruker.getId());
			default:
				return null;
		}
	}

	private void persistDokumentFiler(Journalpost journalpost) {
		List<DokumentFil> dokumentFilList = journalpost.findAllFilDetaljer().stream().map(FilDetaljer::createDokumentFil).collect(Collectors.toList());
		dokumentFilList.forEach(dokumentFilRepository::save);
	}

	private void populerAksjonslogg(Long journalpostId, AksjonsTypeCode aksjon) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElseThrow(JournalpostIkkeFunnetException::new);
		String bruker = null;
		if (!journalpost.getBrukere().isEmpty()) {
			bruker = journalpost.getBrukere().iterator().next().getBrukerId();
		}
		AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(journalpostId)
				.utfoertAv(MDC.get(MDC_CONSUMER_ID))
				.bruker(isNotBlank(bruker) ? bruker : UKJENT)
				.melding("Journalpost " + aksjon.name())
				.build();
		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, emptyList());
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: " + e.getMessage());
		}
	}

	// Bruker eksternReferanseId for å fikse idempodens for spesifikke kanaler
	private Optional<Journalpost> findJournalpostWithIdempodentKanalAlreadyInDb(OpprettJournalpostRequest request) {
		if (isNotBlank(request.getKanal())) {
			if (request.getEksternReferanseId() != null) {
				if (request.isInngaaende()) {
					return joarkRepository.findTopByKanalReferanseId(request.getEksternReferanseId());
				} else { // handtere UTGAAENDE og NOTAT
					final UtsendingsKanalCode kanal = UtsendingsKanalCode.valueOf(request.getKanal());
					if (UTGAAENDE_UTSENDING_IDEMPOTENT_REFERANSE_ID.contains(kanal)) {
						return joarkRepository.findTopByKanalReferanseId(request.getEksternReferanseId());
					}
				}
			}
		}
		return Optional.empty();
	}
}