package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerService;
import no.nav.dokarkiv.core.consumer.aktoer.HentAktoerIdForIdentRequestTo;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
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

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConfig.RETRY_DELAY;
import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConfig.RETRY_MULTIPLIER;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator.validateOppdaterteFelt;

@Service
@Named("oppdaterMetadataJournalpost")
public class OppdaterJournalpostService {

	private static final String APPLIKASJON_FS22 = "FS22";

    private final JoarkRepositorySkjermet joarkRepository;
    private final DokumentinfoRepository dokumentinfoRepository;
	private final JournalpostUpdater journalpostUpdater;
	private final SaksrelasjonUpdater saksrelasjonUpdater;
	private final DokumentInfoUpdater dokumentInfoUpdater;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;
	private final AktoerConsumerService aktoerConsumerService;
	private final HentSakerRepository hentSakerRepository;

	@Inject
    public OppdaterJournalpostService(JoarkRepositorySkjermet joarkRepository,
									  JournalpostUpdater journalpostUpdater,
									  SaksrelasjonUpdater saksrelasjonUpdater,
									  DokumentinfoRepository dokumentinfoRepository,
									  DokumentInfoUpdater dokumentInfoUpdater,
									  LagreAksjonsLoggService lagreAksjonsLoggService,
									  final AktoerConsumerService aktoerConsumerService,
									  final HentSakerRepository hentSakerRepository) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostUpdater = journalpostUpdater;
		this.saksrelasjonUpdater = saksrelasjonUpdater;
		this.dokumentInfoUpdater = dokumentInfoUpdater;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
		this.aktoerConsumerService = aktoerConsumerService;
		this.hentSakerRepository = hentSakerRepository;
	}

	@Retryable(
			include = {ObjectOptimisticLockingFailureException.class, StaleObjectStateException.class},
			backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
	)
	public void oppdaterJournalpost(Long journalpostId, OppdaterJournalpostRequest oppdaterJournalpostRequest) {
		String sakId = null;

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost.getJournalstatus(), journalpost.getJournalposttype());

		if (oppdaterJournalpostRequest.getSak() != null) {
			Sakstype sakstype = oppdaterJournalpostRequest.getSak().getSakstype();
			if((FAGSAK.equals(sakstype) || Sakstype.GENERELL_SAK.equals(sakstype)) && !Fagsaksystem.PP01.equals(oppdaterJournalpostRequest.getSak().getFagsaksystem())){
				sakId = identifiserEllerOpprettArkivsak(oppdaterJournalpostRequest);
			}
		}

		ChangeTracker changeTracker = journalpostUpdater.updateFields(journalpost, oppdaterJournalpostRequest);

		if(!changeTracker.getChanges().isEmpty()) {
			lagreAksjonsLoggService.lagreAksjonsLoggForJournalpost(
					AksjonsTypeCode.ENDRE_METADATA, journalpostId, null,
					hentMeldingFraAksjonsType(AksjonsTypeCode.ENDRE_METADATA), null, changeTracker.getChanges());
		}

		changeTracker = saksrelasjonUpdater.updateFields(journalpost, oppdaterJournalpostRequest, sakId);
		joarkRepository.save(journalpost);
		if(!changeTracker.getChanges().isEmpty()) {
			lagreAksjonsLoggService.lagreAksjonsLoggForJournalpost(
					AksjonsTypeCode.SAKSTILKNYTNING, journalpostId, null,
					hentMeldingFraAksjonsType(AksjonsTypeCode.SAKSTILKNYTNING), null, changeTracker.getChanges());
		}

		if (oppdaterJournalpostRequest.getDokumenter() != null) {
			for (no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokument : oppdaterJournalpostRequest.getDokumenter()) {
				DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokument.getDokumentInfoId()));
				assertDokumentInfoNotNull(dokumentInfo, String.valueOf(journalpost.getJournalpostId()), dokument.getDokumentInfoId());

				changeTracker = dokumentInfoUpdater.updateFields(dokumentInfo, dokument);
				dokumentinfoRepository.save(dokumentInfo);
				if(!changeTracker.getChanges().isEmpty()) {
					lagreAksjonsLoggService.lagreAksjonsLogg(
							AksjonsTypeCode.ENDRE_METADATA, dokumentInfo.getDokumentInfoId(), null,
							hentMeldingFraAksjonsType(AksjonsTypeCode.ENDRE_METADATA), null, changeTracker.getChanges());
				}
			}
		}
	}

	private String hentMeldingFraAksjonsType(AksjonsTypeCode kode) {
		return kode.equals(AksjonsTypeCode.SAKSTILKNYTNING) ?
				"Journalposten ble knyttet til en sak." :
				"Metadata på journalposten ble endret";
	}

	private void assertDokumentInfoNotNull(DokumentInfo dokumentInfo, String journalpostId, String dokumentId) {
		if (dokumentInfo == null) {
			throw new DokumentIkkeFunnetException(String.format("Fant ingen dokument med dokumentId=%s paa journalpost med journalpostId=%s", dokumentId, journalpostId));
		}
	}

	private String identifiserEllerOpprettArkivsak(OppdaterJournalpostRequest request) {
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

	private Sak createSak(OppdaterJournalpostRequest request) {
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
				return aktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(bruker.getId()))
						.getAktoerId();
			default:
				return null;
		}
	}
}
