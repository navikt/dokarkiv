package no.nav.dokarkiv.journalpost.v1.api.sak;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.pdl.PdlIdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AvleveringStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.ArkivsakHarIngenSakerException;
import no.nav.dokarkiv.core.exceptions.SakHarJournalposterUnderRedigeringException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria.SakSearchCriteriaBuilder;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.KassasjonStatusCode.KLAR_FOR_KASSASJON;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AAPEN;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AVBRUTT;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AVSLUTTET;

@Slf4j
@Component
public class AvsluttSakService {

	private static final EnumSet<JournalStatusCode> MIDLERTIDIGE_JOURNALPOSTSTATUSER = EnumSet.of(D, M, MO, OD);
	private static final EnumSet<JournalStatusCode> FERDIGSTILTE_JOURNALPOSTSTATUSER = EnumSet.of(FL, FS, E, R, J);

	private final HentSakerRepository hentSakerRepository;
	private final JournalpostRepository journalpostRepository;
	private final PdlIdentConsumer pdlIdentConsumer;

	public AvsluttSakService(HentSakerRepository hentSakerRepository, JournalpostRepository journalpostRepository, PdlIdentConsumer pdlIdentConsumer) {
		this.hentSakerRepository = hentSakerRepository;
		this.journalpostRepository = journalpostRepository;
		this.pdlIdentConsumer = pdlIdentConsumer;
	}

	@Transactional
	public AvsluttSakStatus avsluttSaker(AvsluttSakRequest avsluttSakRequest) {
		List<Sak> saker = hentSakerForArkivsak(avsluttSakRequest);

		if (saker == null) {
			return AvsluttSakStatus.ALLEREDE_AVBRUTT_AVSLUTTET_ELLER_AVLEVERT;
		}

		List<Long> saksIder = saker.stream().map(Sak::getSakId).toList();
		var tilknyttedeJournalposter = journalpostRepository.fetchBySakIds(saksIder);

		if (tilknyttedeJournalposter.isEmpty()) {
			log.info("Fagsystemsaken har ingen tilknyttede journalposter. Avbryter tilhørende joark-saker.");
			avbrytSaker(saker);
			return AvsluttSakStatus.AVBRUTT;
		}

		if (harSakenAapneJournalposterUnderRedigering(tilknyttedeJournalposter)) {
			throw new SakHarJournalposterUnderRedigeringException("Fagsystemsaken har en eller flere journalposter under redigering og kan ikke avsluttes.");
		}

		if (manglerSakenFerdigstilteJournalposter(tilknyttedeJournalposter)) {
			log.info("Fagsystemsaken har ingen ferdigstilte journalposter. Avbryter joark-saker.");
			avbrytSaker(saker);
			return AvsluttSakStatus.AVBRUTT;
		}

		avsluttSaker(saker, avsluttSakRequest);
		return AvsluttSakStatus.AVSLUTTET;
	}

	private void avbrytSaker(List<Sak> saker) {
		saker.forEach(sak -> {
			sak.setSakStatus(AVBRUTT);
			sak.setKassasjonStatus(KLAR_FOR_KASSASJON);
			sak.setAvleveringStatus(AvleveringStatusCode.AVBRUTT);
			sak.setDatoEndret(LocalDateTime.now());
			sak.setEndretAv(MDC.get(MDC_USER_ID));
		});
	}

	private void avsluttSaker(List<Sak> saker, AvsluttSakRequest avsluttSakRequest) {
		saker.forEach(sak -> {
			sak.setSakStatus(AVSLUTTET);
			sak.setAvleveringStatus(null);
			sak.setKassasjonStatus(null);
			sak.setEndretAv(MDC.get(MDC_USER_ID));
			sak.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			sak.setDatoEndret(LocalDateTime.now());
			sak.setDatoAvsluttet(determineDatoAvsluttet(avsluttSakRequest));
			sak.setAdministrativEnhet(avsluttSakRequest.getAdministrativEnhet());
			sak.setDatoSakOpprettet(avsluttSakRequest.getOpprettetDato());
			sak.setSakAnsvarlig(determineSakAnsvarlig(avsluttSakRequest));
			sak.setAvsluttetAv(MDC.get(MDC_USER_ID));
			sak.setAvsluttetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		});
	}

	private LocalDateTime determineDatoAvsluttet(AvsluttSakRequest avsluttSakRequest) {
		return avsluttSakRequest.getAvsluttetDato() == null ? LocalDateTime.now() : avsluttSakRequest.getAvsluttetDato();
	}

	private String determineSakAnsvarlig(AvsluttSakRequest avsluttSakRequest) {
		String sakAnsvarlig = avsluttSakRequest.sakAnsvarlig;

		if (sakAnsvarlig == null || sakAnsvarlig.isBlank()) {
			return avsluttSakRequest.getAdministrativEnhet();
		} else {
			return sakAnsvarlig;
		}
	}

	private boolean harSakenAapneJournalposterUnderRedigering(List<Journalpost> journalposts) {
		return journalposts.stream()
				.anyMatch(journalpost -> MIDLERTIDIGE_JOURNALPOSTSTATUSER.contains(journalpost.getJournalstatus()) && !journalpost.isFeilregistrert());
	}

	private boolean manglerSakenFerdigstilteJournalposter(List<Journalpost> journalposts) {
		return journalposts.stream()
				.noneMatch(journalpost -> FERDIGSTILTE_JOURNALPOSTSTATUSER.contains(journalpost.getJournalstatus()));
	}

	private List<Sak> hentSakerForArkivsak(AvsluttSakRequest avsluttSakRequest) {
		SakSearchCriteria criteria = generateSakSearchCriteria(avsluttSakRequest);
		var saker = hentSakerRepository.finnSaker(criteria);

		if (saker.isEmpty()) {
			throw new ArkivsakHarIngenSakerException("Fant ingen saker for arkivsak med fagsakId=%s og fagsaksystem=%s".formatted(avsluttSakRequest.getFagsakId(), avsluttSakRequest.getFagsaksystem()));
		}

		if (erArkivsakenAlleredeAvsluttet(saker)) {
			return null;
		}

		return saker;
	}

	private static boolean erArkivsakenAlleredeAvsluttet(List<Sak> saker) {
		var antallAapneSaker = saker.stream()
				.filter(sak -> (sak.getSakStatus() == null || AAPEN == sak.getSakStatus()))
				.count();

		return antallAapneSaker < saker.size();
	}

	private SakSearchCriteria generateSakSearchCriteria(AvsluttSakRequest avsluttSakRequest) {
		return switch (avsluttSakRequest.getBruker().getIdType()) {
			case ORGNR -> generateOrganisasjonSakCriteria(avsluttSakRequest);
			case AKTOERID, FNR -> {
				var aktoerIds = pdlIdentConsumer.hentAlleAktoerIdsForIdent(avsluttSakRequest.getBruker().getId());
				yield generateAktoerIdCriteria(avsluttSakRequest, aktoerIds);
			}
		};
	}

	private SakSearchCriteria generateAktoerIdCriteria(AvsluttSakRequest avsluttSakRequest, List<String> aktoerIds) {
		return generateBaseSakSearchCriteria(avsluttSakRequest)
				.aktoerId(aktoerIds)
				.build();
	}

	private SakSearchCriteria generateOrganisasjonSakCriteria(AvsluttSakRequest avsluttSakRequest) {
		return generateBaseSakSearchCriteria(avsluttSakRequest)
				.orgnr(avsluttSakRequest.getBruker().getId())
				.build();
	}

	private SakSearchCriteriaBuilder generateBaseSakSearchCriteria(AvsluttSakRequest avsluttSakRequest) {
		return SakSearchCriteria.builder()
				.tema(singletonList(avsluttSakRequest.getTema()))
				.fagsakNr(avsluttSakRequest.fagsakId)
				.applikasjon(avsluttSakRequest.getFagsaksystem());
	}
}
