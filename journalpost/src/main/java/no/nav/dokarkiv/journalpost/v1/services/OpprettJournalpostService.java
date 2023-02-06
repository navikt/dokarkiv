package no.nav.dokarkiv.journalpost.v1.services;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO.arkivElementEndringNew;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OVERSTYR_INNSYN;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.util.JournalpostApiMetrics.incrementSakstypeCounter;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service(value = "opprettNyJournalpostService")
@Slf4j
public class OpprettJournalpostService {

	public static final String UKJENT = "UKJENT";
	private static final String APPLIKASJON_FS22 = "FS22";
	private static final String SKANMOTOVRIG = "srvskanmotovrig";

	private final JournalpostRepository journalpostRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final OpprettJournalpostApiRequestMapper opprettJournalpostApiRequestMapper;
	private final DefaultSporingPopulator defaultSporingPopulator;
	private final AksjonsLoggService aksjonsLoggService;
	private final IdentConsumer identConsumer;
	private final HentSakerRepository hentSakerRepository;
	private final OpprettJournalpostPDFAUtils opprettJournalpostPDFAUtils;
	private final MeterRegistry meterRegistry;

	public OpprettJournalpostService(final JournalpostRepository journalpostRepository,
									 final DokumentFilRepository dokumentFilRepository,
									 final OpprettJournalpostApiRequestMapper opprettJournalpostApiRequestMapper,
									 final DefaultSporingPopulator defaultSporingPopulator,
									 final AksjonsLoggService aksjonsLoggService,
									 final IdentConsumer identConsumer,
									 final HentSakerRepository hentSakerRepository,
									 final OpprettJournalpostPDFAUtils opprettJournalpostPDFAUtils,
									 final MeterRegistry meterRegistry) {
		this.journalpostRepository = journalpostRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.opprettJournalpostApiRequestMapper = opprettJournalpostApiRequestMapper;
		this.defaultSporingPopulator = defaultSporingPopulator;
		this.aksjonsLoggService = aksjonsLoggService;
		this.identConsumer = identConsumer;
		this.hentSakerRepository = hentSakerRepository;
		this.opprettJournalpostPDFAUtils = opprettJournalpostPDFAUtils;
		this.meterRegistry = meterRegistry;
	}

	public OpprettJournalpostResult opprettJournalpost(OpprettJournalpostRequest request) {
		final String eksternReferanseId = request.getEksternReferanseId();
		boolean journalpostExists = isJournalpostExists(eksternReferanseId);
		if (journalpostExists) {
			Optional<Journalpost> existingJournalpost = findJournalpostByEksternReferanseId(eksternReferanseId);
			if(existingJournalpost.isPresent()) {
				final Journalpost journalpost = existingJournalpost.get();
				log.warn("Journalpost med eksternReferanseId={} for kanal={} finnes fra før. Oppretter ikke ny journalpost.", eksternReferanseId, journalpost.getMottakskanal());
				return new OpprettJournalpostResult(journalpost, true);
			}
		}

		Optional<Sak> sakOptional = hentSak(request);
		Long sakId = sakOptional.map(Sak::getSakId).orElse(null);

		Journalpost journalpost = opprettJournalpostApiRequestMapper.map(request, sakId);
		defaultSporingPopulator.populateSporingInfo(journalpost, MDC.get(MDCConstants.MDC_USER_NAME));
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn()));

		persistDokumentFiler(journalpost);

		journalpostRepository.persist(journalpost);

		populerAksjonsloggFromChanges(journalpost.getJournalpostId(), sakOptional);

		log.info(MDC.get(MDC_REQUEST_ID) + " har opprettet ny journalpost, journalpostId={} og status={}", journalpost.getJournalpostId(), journalpost.getJournalstatus());

		if (!SKANMOTOVRIG.equalsIgnoreCase(journalpost.getOpprettetAvNavn())) {
			opprettJournalpostPDFAUtils.safeValidateAndLogPDFA(journalpost);
		}

		return new OpprettJournalpostResult(journalpost, false);
	}

	private Optional<Sak> hentSak(OpprettJournalpostRequest request) {
		if (request.getSak() != null) {
			Sakstype sakstype = request.getSak().getSakstype();
			incrementSakstypeCounter(sakstype, "opprettjournalpost", meterRegistry);
			if ((FAGSAK.equals(sakstype) || Sakstype.GENERELL_SAK.equals(sakstype)) && !Fagsaksystem.PP01.equals(request.getSak().getFagsaksystem())) {
				return Optional.of(identifiserEllerOpprettArkivsak(request));
			}
		}
		return Optional.empty();
	}

	private Sak identifiserEllerOpprettArkivsak(OpprettJournalpostRequest request) {
		Sak sak = createSak(request);
		List<Sak> saker = hentSakerRepository.finnSaker(SakSearchCriteria.builder()
				.aktoerId(sak.getAktoerId())
				.orgnr(sak.getOrgnr())
				.tema(singletonList(sak.getTema()))
				.applikasjon(sak.getApplikasjon())
				.fagsakNr(sak.getFagsakNr())
				.build());
		if (saker.isEmpty()) {
			return hentSakerRepository.lagre(sak);
		} else {
			return saker.stream().max(Comparator.comparing(Sak::getSakId)).orElseThrow(UgyldigInputException::new);
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
		List<DokumentFil> dokumentFilList = journalpost.findAllFilDetaljer().stream().map(FilDetaljer::createDokumentFil).toList();
		dokumentFilRepository.persistAll(dokumentFilList);
	}

	private void populerAksjonsloggFromChanges(Long journalpostId, Optional<Sak> sakOptional) {
		final Journalpost journalpost = journalpostRepository.findById(journalpostId).orElseThrow(JournalpostIkkeFunnetException::new);
		final String brukerId = journalpost.getBrukere().stream()
				.findFirst()
				.map(no.nav.dokarkiv.core.domain.entities.Bruker::getBrukerId)
				.orElse(null);

		populerAksjonslogg(journalpostId, OPPRETT, brukerId, Stream.of(
						arkivElementEndringNew("Journalpost.fagomrade",
								journalpost.getFagomrade() != null ? journalpost.getFagomrade().name() : null),
						arkivElementEndringNew("Journalpost.innhold", journalpost.getInnhold()),
						arkivElementEndringNew("Journalpost.avsend_mottaker", journalpost.getAvsenderMottaker()),
						arkivElementEndringNew("Journalpost.avsend_mottak_id", journalpost.getAvsenderMottakerId()),
						arkivElementEndringNew("Journalpost.journalf_enhet", journalpost.getJournalForendeEnhetId()),
						arkivElementEndringNew("Bruker.bruker_id", brukerId)
				).filter(elementEndring -> Objects.nonNull(elementEndring.getTilVerdi()))
				.toList());

		if (journalpost.getInnsyn() != null) {
			populerAksjonslogg(journalpostId, OVERSTYR_INNSYN, brukerId, singletonList(
					arkivElementEndringNew("Journalpost.k_innsyn", journalpost.getInnsyn().toString())
			));
		}

		sakOptional.ifPresent(sak -> populerAksjonslogg(journalpostId, SAKSTILKNYTNING, brukerId, Stream.of(
						arkivElementEndringNew("Saksrelasjon.sakId", journalpost.getSaksrelasjon().getSakId().toString()),
						arkivElementEndringNew("Saksrelasjon.fagsystem",
								journalpost.getSaksrelasjon().getFagsystem() != null ? journalpost.getSaksrelasjon().getFagsystem().name() : null),
						arkivElementEndringNew("Sak.fagsaknr", sak.getFagsakNr()),
						arkivElementEndringNew("Sak.applikasjon", sak.getApplikasjon())
				).filter(elementEndring -> Objects.nonNull(elementEndring.getTilVerdi()))
				.toList()));
	}

	private void populerAksjonslogg(Long journalpostId, AksjonsTypeCode aksjon, String bruker, List<ArkivElementEndringTO> aksjonsloggendringer) {
		AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(journalpostId)
				.bruker(isNotBlank(bruker) ? bruker : UKJENT)
				.melding(mapAksjonsloggmelding(aksjon))
				.build();

		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, aksjonsloggendringer);
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: " + e.getMessage());
		}
	}

	private static String mapAksjonsloggmelding(AksjonsTypeCode aksjon) {
		if (OVERSTYR_INNSYN.equals(aksjon)) {
			return "Innsynsreglene ble overstyrt ved opprettelse av journalpost";
		} else {
			return "Journalpost " + aksjon;
		}
	}

	private boolean isJournalpostExists(String eksternReferanseId) {
		return isNotBlank(eksternReferanseId) && journalpostRepository.existsByKanalReferanseId(eksternReferanseId);
	}
	private Optional<Journalpost> findJournalpostByEksternReferanseId(String eksternReferanseId) {
		//eksternReferanseId == kanalReferanseId
		return isBlank(eksternReferanseId) ? Optional.empty() : journalpostRepository.findByKanalReferanseId(eksternReferanseId);
	}
}