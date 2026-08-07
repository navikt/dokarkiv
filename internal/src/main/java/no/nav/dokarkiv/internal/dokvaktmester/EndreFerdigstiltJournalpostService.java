package no.nav.dokarkiv.internal.dokvaktmester;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.consumer.pdl.PdlIdentConsumer;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.InvalidBrukerException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.SakIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.BRUKER_BRUKER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_SAKID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_METADATA;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SAKSTILKNYTNING;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.internal.dokvaktmester.EndreFerdigstiltJournalpostValidator.validateJournalpostIsFerdigstilt;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class EndreFerdigstiltJournalpostService {
	static final EnumSet<JournalStatusCode> FERDIGSTILTE_STATUSER = EnumSet.of(J, FS, FL, E);
	static final int FOLKEREGISTERIDENT_LENGTH = 11;

	private final JournalpostRepository journalpostRepository;
	private final SakRepository sakRepository;
	private final HentSakerRepository hentSakerRepository;
	private final PdlIdentConsumer pdlIdentConsumer;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;

	public EndreFerdigstiltJournalpostService(JournalpostRepository journalpostRepository,
											  SakRepository sakRepository,
											  HentSakerRepository hentSakerRepository,
											  PdlIdentConsumer pdlIdentConsumer,
											  LagreAksjonsLoggService lagreAksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.sakRepository = sakRepository;
		this.hentSakerRepository = hentSakerRepository;
		this.pdlIdentConsumer = pdlIdentConsumer;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
	}

	@Transactional
	public void endreFerdigstiltJournalpost(long journalpostId, EndreFerdigstiltJournalpostRequest request) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Fant ikke journalpostId=" + journalpostId));
		validateJournalpostIsFerdigstilt(journalpost);
		Sak tilknyttetSak = hentSakerRepository.hentSak(journalpost.getSaksrelasjon().getSakId())
				.orElseThrow(() -> new SakIkkeFunnetException("Fant ikke tilknyttetSak for journalpostId=" + journalpostId));
		String aktoerId = hentAktoerId(request.brukerId(), tilknyttetSak);
		SakEndring sakEndring = SakEndring.opprett(request, aktoerId, tilknyttetSak);
		Sak nySak = brukEksisterendeSak(sakEndring)
				.orElse(opprettNySak(sakEndring));
		tilknyttNySak(journalpost, nySak, sakEndring.begrunnelseNokkel());
		oppdaterBruker(journalpost, sakEndring);
	}

	private Optional<Sak> brukEksisterendeSak(SakEndring sakEndring) {
		List<Sak> saker = hentSakerRepository.finnSaker(SakSearchCriteria.builder()
				.tema(List.of(sakEndring.tema()))
				.aktoerId(List.of(sakEndring.aktoerId()))
				.fagsakNr(sakEndring.fagsakId())
				.applikasjon(sakEndring.fagsaksystem())
				.build());
		if (saker.isEmpty()) {
			log.info("Fant ingen eksisterende saker. Oppretter ny sak med tema={} for begrunnelse={}",
					sakEndring.tema(), sakEndring.begrunnelseNokkel());
			return Optional.empty();
		}
		Sak eksisterendeSak = saker.getFirst();
		log.info("Fant {} eksisterende saker. Bruker eksisterende sakId={}, tema={} for begrunnelse={}",
				saker.size(), eksisterendeSak.getSakId(),
				sakEndring.tema(), sakEndring.begrunnelseNokkel());
		return Optional.of(eksisterendeSak);
	}

	private Sak opprettNySak(SakEndring sakEndring) {
		Sak nySak = Sak.builder()
				.aktoerId(sakEndring.aktoerId())
				.tema(sakEndring.tema())
				.fagsakNr(sakEndring.fagsakId())
				.applikasjon(sakEndring.fagsaksystem())
				.opprettetAv(sakEndring.begrunnelseNokkel())
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
		return sakRepository.persist(nySak);
	}

	private void tilknyttNySak(Journalpost journalpost, Sak nySak, String begrunnelseNokkel) {
		Long sakId = journalpost.getSaksrelasjon().getSakId();
		Long nySakId = nySak.getSakId();
		Saksrelasjon saksrelasjon = journalpost.getSaksrelasjon();
		saksrelasjon.setSakId(nySakId);
		saksrelasjon.setEndretKildeNavn(begrunnelseNokkel);
		lagreAksjonsLoggService.lagreAksjonsLoggForJournalpost(SAKSTILKNYTNING, journalpost.getJournalpostId(), null,
				"Journalposten ble knyttet til en annen sak.",
				begrunnelseNokkel,
				List.of(ArkivElementEndringTO.builder()
						.arkivElement(SAKSRELASJON_SAKID)
						.fraVerdi(sakId.toString())
						.tilVerdi(nySakId.toString())
						.build()));
		log.info("Oppdaterer til sakId={} fra sakId={} for journalpostId={}", nySakId, sakId, journalpost.getJournalpostId());
	}

	private String hentAktoerId(String brukerId, Sak tilknyttetSak) {
		if (!isBlank(brukerId)) {
			if (brukerId.length() == FOLKEREGISTERIDENT_LENGTH) {
				return pdlIdentConsumer.hentAktoerId(brukerId);
			} else {
				throw new UnsupportedOperationException("brukerId som ikke er folkeregisterident eller aktørId støttes ikke");
			}
		} else {
			return tilknyttetSak.getAktoerId();
		}
	}

	private void oppdaterBruker(Journalpost journalpost, SakEndring sakEndring) {
		if (!isBlank(sakEndring.brukerId())) {
			Bruker bruker = journalpost.getBrukere().stream().max(Comparator.comparing(Bruker::getBrukerInfoId))
					.orElseThrow(() -> new InvalidBrukerException("Finner ingen Bruker"));
			if (!sakEndring.brukerId().equals(bruker.getBrukerId())) {
				String brukerId = bruker.getBrukerId();
				bruker.setBrukerId(sakEndring.brukerId());
				bruker.setEndretKildeNavn(sakEndring.begrunnelseNokkel());
				lagreAksjonsLoggService.lagreAksjonsLoggForJournalpost(ENDRE_METADATA, journalpost.getJournalpostId(), null,
						"Journalpost har endret bruker",
						sakEndring.begrunnelseNokkel(),
						List.of(ArkivElementEndringTO.builder()
								.arkivElement(BRUKER_BRUKER_ID)
								.fraVerdi(brukerId)
								.tilVerdi(sakEndring.brukerId())
								.build()));
			}
			log.info("Oppdaterer bruker for journalpostId={}", journalpost.getJournalpostId());
		}
	}
}
