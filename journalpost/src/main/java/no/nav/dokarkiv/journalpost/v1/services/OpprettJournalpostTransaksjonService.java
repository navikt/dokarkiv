package no.nav.dokarkiv.journalpost.v1.services;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import no.nav.dokarkiv.core.sporing.DefaultSporingPopulator;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.mappers.OpprettJournalpostApiRequestMapper;
import no.nav.dokarkiv.journalpost.v1.util.opprettjournalpost.OpprettJournalpostPDFAUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.BRUKER_BRUKER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_AVSENDER_MOTTAKER;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_FAGOMRADE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_INNHOLD;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALFORENDE_ENHET;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_OVERSTYR_INNSYN;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_FAGSYSTEM;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_SAKID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAK_APPLIKASJON;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAK_FAGSAKNR;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO.arkivElementEndringNew;
import static no.nav.dokarkiv.core.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostOppslagService.skalIdentifisereEllerOppretteArkivsak;
import static no.nav.dokarkiv.journalpost.v1.util.JournalpostApiMetrics.incrementSakstypeCounter;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/// Databasedelen av opprettelse av journalpost. Skal ikke gjøre eksterne web-kall,
/// disse gjøres på forhånd av [OpprettJournalpostOppslagService].
@Service
@Slf4j
public class OpprettJournalpostTransaksjonService {

	private static final String APPLIKASJON_FS22 = "FS22";

	private final JournalpostRepository journalpostRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final OpprettJournalpostApiRequestMapper opprettJournalpostApiRequestMapper;
	private final DefaultSporingPopulator defaultSporingPopulator;
	private final AksjonsLoggService aksjonsLoggService;
	private final HentSakerRepository hentSakerRepository;
	private final MeterRegistry meterRegistry;
	private final OpprettJournalpostPDFAUtils opprettJournalpostPDFAUtils;

	public OpprettJournalpostTransaksjonService(final JournalpostRepository journalpostRepository,
												final DokumentFilRepository dokumentFilRepository,
												final OpprettJournalpostApiRequestMapper opprettJournalpostApiRequestMapper,
												final DefaultSporingPopulator defaultSporingPopulator,
												final AksjonsLoggService aksjonsLoggService,
												final HentSakerRepository hentSakerRepository,
												final MeterRegistry meterRegistry,
												final OpprettJournalpostPDFAUtils opprettJournalpostPDFAUtils) {
		this.journalpostRepository = journalpostRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.opprettJournalpostApiRequestMapper = opprettJournalpostApiRequestMapper;
		this.defaultSporingPopulator = defaultSporingPopulator;
		this.aksjonsLoggService = aksjonsLoggService;
		this.hentSakerRepository = hentSakerRepository;
		this.meterRegistry = meterRegistry;
		this.opprettJournalpostPDFAUtils = opprettJournalpostPDFAUtils;
	}

	@Transactional(readOnly = true)
	public Optional<OpprettJournalpostResult> finnEksisterendeJournalpost(String eksternReferanseId) {
		return finnEksisterende(eksternReferanseId);
	}

	@Transactional
	public OpprettJournalpostResult opprettJournalpost(OpprettJournalpostRequest request, OpprettJournalpostOppslag oppslag) {
		Optional<OpprettJournalpostResult> eksisterende = finnEksisterende(request.getEksternReferanseId());
		if (eksisterende.isPresent()) {
			return eksisterende.get();
		}

		Optional<Sak> sakOptional = hentSak(request, oppslag);
		Long sakId = sakOptional.map(Sak::getSakId).orElse(null);

		Journalpost journalpost = opprettJournalpostApiRequestMapper.map(request, sakId, oppslag);
		defaultSporingPopulator.populateSporingInfo(journalpost, MDC.get(MDCConstants.MDC_USER_NAME));
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn()));

		persistDokumentFiler(journalpost);

		journalpostRepository.persist(journalpost);

		populerAksjonsloggFromChanges(journalpost.getJournalpostId(), sakOptional);

		opprettJournalpostPDFAUtils.safeValidateAndLogPDFA(journalpost);

		log.info(MDC.get(MDC_REQUEST_ID) + " har opprettet ny journalpost, journalpostId={} og status={}", journalpost.getJournalpostId(), journalpost.getJournalstatus());

		return OpprettJournalpostResult.fra(false, journalpost);
	}

	private Optional<OpprettJournalpostResult> finnEksisterende(String eksternReferanseId) {
		if (!isJournalpostExists(eksternReferanseId)) {
			return Optional.empty();
		}
		return findJournalpostByEksternReferanseId(eksternReferanseId)
				.map(existingJournalpost -> {
					log.warn("Journalpost med eksternReferanseId={} for kanal={} finnes fra før. Oppretter ikke ny journalpost.", eksternReferanseId, existingJournalpost.getMottakskanal());
					return OpprettJournalpostResult.fra(true, existingJournalpost);
				});
	}

	private Optional<Sak> hentSak(OpprettJournalpostRequest request, OpprettJournalpostOppslag oppslag) {
		if (request.getSak() != null) {
			incrementSakstypeCounter(request.getSak().getSakstype(), "opprettjournalpost", meterRegistry);

			if (skalIdentifisereEllerOppretteArkivsak(request)) {
				return Optional.of(identifiserEllerOpprettArkivsak(request, oppslag.aktoerIdForSak()));
			}
		}
		return Optional.empty();
	}

	private Sak identifiserEllerOpprettArkivsak(OpprettJournalpostRequest request, String aktoerId) {
		Sak sak = createSak(request, aktoerId);
		List<Sak> saker = hentSakerRepository.finnSaker(SakSearchCriteria.builder()
				.aktoerId(singletonList(sak.getAktoerId()))
				.orgnr(sak.getOrgnr())
				.tema(singletonList(sak.getTema()))
				.applikasjon(sak.getApplikasjon())
				.fagsakNr(sak.getFagsakNr())
				.build());

		if (saker.isEmpty()) {
			return hentSakerRepository.lagre(sak);
		} else {
			return saker.getFirst(); // Hent eldste sak
		}
	}

	private Sak createSak(OpprettJournalpostRequest request, String aktoerId) {
		return Sak.builder()
				.aktoerId(aktoerId)
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
						arkivElementEndringNew(JOURNALPOST_FAGOMRADE, journalpost.getFagomrade() != null ? journalpost.getFagomrade().name() : null),
						arkivElementEndringNew(JOURNALPOST_INNHOLD, journalpost.getInnhold()),
						arkivElementEndringNew(JOURNALPOST_AVSENDER_MOTTAKER, journalpost.getAvsenderMottaker()),
						arkivElementEndringNew(JOURNALPOST_AVSENDER_MOTTAKER_ID, journalpost.getAvsenderMottakerId()),
						arkivElementEndringNew(JOURNALPOST_JOURNALFORENDE_ENHET, journalpost.getJournalForendeEnhetId()),
						arkivElementEndringNew(BRUKER_BRUKER_ID, brukerId),
						arkivElementEndringNew(JOURNALPOST_OVERSTYR_INNSYN, journalpost.getInnsyn() != null ? journalpost.getInnsyn().name() : null)
				).filter(elementEndring -> Objects.nonNull(elementEndring.getTilVerdi()))
				.toList());

		sakOptional.ifPresent(sak -> populerAksjonslogg(journalpostId, SAKSTILKNYTNING, brukerId, Stream.of(
						arkivElementEndringNew(SAKSRELASJON_SAKID, journalpost.getSaksrelasjon().getSakId().toString()),
						arkivElementEndringNew(SAKSRELASJON_FAGSYSTEM, journalpost.getSaksrelasjon().getFagsystem() != null ? journalpost.getSaksrelasjon().getFagsystem().name() : null),
						arkivElementEndringNew(SAK_FAGSAKNR, sak.getFagsakNr()),
						arkivElementEndringNew(SAK_APPLIKASJON, sak.getApplikasjon())
				).filter(elementEndring -> Objects.nonNull(elementEndring.getTilVerdi()))
				.toList()));
	}

	private void populerAksjonslogg(Long journalpostId, AksjonsTypeCode aksjon, String bruker, List<ArkivElementEndringTO> aksjonsloggendringer) {
		AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(journalpostId)
				.bruker(isNotBlank(bruker) ? bruker : OpprettJournalpostService.UKJENT)
				.melding("Journalpost " + aksjon)
				.build();

		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, aksjonsloggendringer);
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: " + e.getMessage());
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
