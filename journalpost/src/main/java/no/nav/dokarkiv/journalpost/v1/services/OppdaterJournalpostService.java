package no.nav.dokarkiv.journalpost.v1.services;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.ChangeTracker;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.DokumentInfoUpdater;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.SaksrelasjonUpdater;
import org.hibernate.StaleObjectStateException;
import org.slf4j.MDC;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_METADATA;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SAKSTILKNYTNING;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.ALTINN;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.EESSI;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO_CHAT;
import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConfig.RETRY_DELAY;
import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConfig.RETRY_MULTIPLIER;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static no.nav.dokarkiv.journalpost.v1.util.JournalpostApiMetrics.incrementOppdateringAvAvsenderMedDigitalMottakskanalCounter;
import static no.nav.dokarkiv.journalpost.v1.util.JournalpostApiMetrics.incrementSakstypeCounter;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator.validateOppdaterteFelt;

@Service("oppdaterMetadataJournalpost")
@Slf4j
public class OppdaterJournalpostService {

	private static final String APPLIKASJON_FS22 = "FS22";
	private static final EnumSet<MottaksKanalCode> DIGITALE_KANALER = EnumSet.of(NAV_NO, NAV_NO_CHAT, ALTINN, EESSI);

	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final JournalpostUpdater journalpostUpdater;
	private final SaksrelasjonUpdater saksrelasjonUpdater;
	private final DokumentInfoUpdater dokumentInfoUpdater;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;
	private final AksjonsLoggService aksjonsLoggService;
	private final IdentConsumer identConsumer;
	private final HentSakerRepository hentSakerRepository;
	private final MeterRegistry meterRegistry;

	public OppdaterJournalpostService(JournalpostRepositorySkjermet journalpostRepositorySkjermet,
									  JournalpostUpdater journalpostUpdater,
									  SaksrelasjonUpdater saksrelasjonUpdater,
									  DokumentInfoUpdater dokumentInfoUpdater,
									  LagreAksjonsLoggService lagreAksjonsLoggService,
									  AksjonsLoggService aksjonsLoggService,
									  final IdentConsumer identConsumer,
									  final HentSakerRepository hentSakerRepository,
									  final MeterRegistry meterRegistry) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.journalpostUpdater = journalpostUpdater;
		this.saksrelasjonUpdater = saksrelasjonUpdater;
		this.dokumentInfoUpdater = dokumentInfoUpdater;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.identConsumer = identConsumer;
		this.hentSakerRepository = hentSakerRepository;
		this.meterRegistry = meterRegistry;
	}

	@Retryable(
			include = {ObjectOptimisticLockingFailureException.class, StaleObjectStateException.class},
			backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
	)
	public void oppdaterJournalpost(Long journalpostId, OppdaterJournalpostRequest oppdaterJournalpostRequest) {
		Long sakId = null;

		Journalpost journalpost = journalpostRepositorySkjermet.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		if (oppdateringAvAvsenderMedDigitalMottakskanal(oppdaterJournalpostRequest, journalpost)) {
			log.info("Avsender på digitalt innsendt journalpost med mottakskanal={} ble oppdatert", journalpost.getMottakskanal());
			incrementOppdateringAvAvsenderMedDigitalMottakskanalCounter(meterRegistry);
		}

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);

		if (oppdaterJournalpostRequest.getSak() != null) {
			Sakstype sakstype = oppdaterJournalpostRequest.getSak().getSakstype();
			incrementSakstypeCounter(sakstype, "oppdaterjournalpost", meterRegistry);
			if ((FAGSAK.equals(sakstype) || GENERELL_SAK.equals(sakstype)) && !PP01.equals(oppdaterJournalpostRequest.getSak().getFagsaksystem())) {
				sakId = identifiserEllerOpprettArkivsak(oppdaterJournalpostRequest);
			}
		}

		ChangeTracker changeTracker = saksrelasjonUpdater.updateFields(journalpost, oppdaterJournalpostRequest, sakId);
		if (!changeTracker.getChanges().isEmpty()) {
			lagreAksjonsLoggService.lagreAksjonsLoggForJournalpost(
					SAKSTILKNYTNING, journalpostId, null,
					hentMeldingFraAksjonsType(SAKSTILKNYTNING), null, changeTracker.getChanges());
		}

		changeTracker = journalpostUpdater.updateFields(journalpost, oppdaterJournalpostRequest);
		if (!changeTracker.getChanges().isEmpty()) {
			lagreAksjonsLoggService.lagreAksjonsLoggForJournalpost(
					ENDRE_METADATA, journalpostId, null,
					hentMeldingFraAksjonsType(ENDRE_METADATA), null, changeTracker.getChanges());
		}

		if (oppdaterJournalpostRequest.getDokumenter() != null) {
			for (no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokument : oppdaterJournalpostRequest.getDokumenter()) {
				DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokument.getDokumentInfoId()));
				assertDokumentInfoNotNull(dokumentInfo, String.valueOf(journalpost.getJournalpostId()), dokument.getDokumentInfoId());

				changeTracker = dokumentInfoUpdater.updateFields(dokumentInfo, dokument);
				if (!changeTracker.getChanges().isEmpty()) {
					lagreAksjonsLoggService.lagreAksjonsLogg(
							ENDRE_METADATA, dokumentInfo.getDokumentInfoId(), null,
							hentMeldingFraAksjonsType(ENDRE_METADATA), null, changeTracker.getChanges());
				}
			}
		}
	}

	public void knyttTilAnnenSakOppdaterJournalpost(Long journalpostId, OppdaterJournalpostRequest oppdaterJournalpostRequest) {
		Long sakId = null;

		Journalpost journalpost = journalpostRepositorySkjermet.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost);

		if (oppdaterJournalpostRequest.getSak() != null) {
			Sakstype sakstype = oppdaterJournalpostRequest.getSak().getSakstype();
			incrementSakstypeCounter(sakstype, "oppdaterjournalpost", meterRegistry);
			if ((FAGSAK.equals(sakstype) || GENERELL_SAK.equals(sakstype)) && !PP01.equals(oppdaterJournalpostRequest.getSak().getFagsaksystem())) {
				sakId = identifiserEllerOpprettArkivsak(oppdaterJournalpostRequest);
			}
		}

		ChangeTracker changeTracker = journalpostUpdater.updateFields(journalpost, oppdaterJournalpostRequest);
		if (!changeTracker.getChanges().isEmpty()) {
			populerAksjonslogg(journalpostId, ENDRE_METADATA,changeTracker.getChanges());
		}

		changeTracker = saksrelasjonUpdater.updateFields(journalpost, oppdaterJournalpostRequest, sakId);
		if (!changeTracker.getChanges().isEmpty()) {
			populerAksjonslogg(journalpostId, SAKSTILKNYTNING, changeTracker.getChanges());
		}
	}

	private void populerAksjonslogg(Long journalpostId, AksjonsTypeCode aksjon, List<ArkivElementEndringTO> endringer) {
		AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(journalpostId)
				.hjemmel(null)
				.melding(hentMeldingFraAksjonsType(aksjon))
				.build();
		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, endringer);
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: " + e.getMessage());
		}
	}

	private String hentMeldingFraAksjonsType(AksjonsTypeCode kode) {
		return kode.equals(SAKSTILKNYTNING) ?
				"Journalposten ble knyttet til en sak." :
				"Metadata på journalposten ble endret";
	}

	private void assertDokumentInfoNotNull(DokumentInfo dokumentInfo, String journalpostId, String dokumentId) {
		if (dokumentInfo == null) {
			throw new DokumentIkkeFunnetException(String.format("Fant ingen dokument med dokumentId=%s paa journalpost med journalpostId=%s", dokumentId, journalpostId));
		}
	}

	private Long identifiserEllerOpprettArkivsak(OppdaterJournalpostRequest request) {
		Sak sak = createSak(request);
		List<Sak> saker = hentSakerRepository.finnSaker(SakSearchCriteria.builder()
				.aktoerId(sak.getAktoerId())
				.orgnr(sak.getOrgnr())
				.tema(Collections.singletonList(sak.getTema()))
				.applikasjon(sak.getApplikasjon())
				.fagsakNr(sak.getFagsakNr())
				.build());
		if (saker.isEmpty()) {
			return hentSakerRepository.lagre(sak).getSakId();
		} else {
			var valgtSak = saker.stream().map(Sak::getSakId).max(Comparator.naturalOrder()).orElseThrow(UgyldigInputException::new);

			var listeMedSakId = saker.stream().map(Sak::getSakId).toList();
			var harDuplikateSaker = listeMedSakId.size() > 1;

			if (harDuplikateSaker) {
				log.info("OppdaterJournalpostService/knyttTilAnnenSak har duplikate saker={}. Velger den nyeste saken={}", listeMedSakId, valgtSak);
			}

			return valgtSak;
		}
	}

	private Sak createSak(OppdaterJournalpostRequest request) {
		return Sak.builder()
				.aktoerId(hentAktoerId(request.getBruker()))
				.orgnr(ORGNR.equals(request.getBruker().getIdType()) ?
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
		return switch (bruker.getIdType()) {
			case AKTOERID -> bruker.getId();
			case FNR -> identConsumer.hentAktoerId(bruker.getId());
			default -> null;
		};
	}

	private static boolean oppdateringAvAvsenderMedDigitalMottakskanal(OppdaterJournalpostRequest oppdaterJournalpostRequest, Journalpost journalpost) {
		return (DIGITALE_KANALER.contains(journalpost.getMottakskanal()) && oppdaterJournalpostRequest.getAvsenderMottaker() != null) &&
				(oppdaterJournalpostRequest.getAvsenderMottaker().getNavn() != null || oppdaterJournalpostRequest.getAvsenderMottaker().getId() != null);
	}
}
