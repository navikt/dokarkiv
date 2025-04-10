package no.nav.dokarkiv.journalpost.v1.api.sak;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.pdl.PdlIdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AvleveringStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.SakHarJournalposterUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.SakIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
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

	private final HentSakerRepository hentSakerRepository;
	private final JournalpostRepository journalpostRepository;
	private final PdlIdentConsumer pdlIdentConsumer;
	private final EnumSet<JournalStatusCode> midlertidigeJournalpostStatuser = EnumSet.of(R, D, M, MO, OD);
	private final EnumSet<JournalStatusCode> ferdigstilteJournalpostStatuser = EnumSet.of(FL, FS, E, J);
	private static final String AVSLUTTET_STRING = "avsluttet";
	private static final String AVBRUTT_STRING = "avbrutt";

	public AvsluttSakService(HentSakerRepository hentSakerRepository, JournalpostRepository journalpostRepository, PdlIdentConsumer pdlIdentConsumer) {
		this.hentSakerRepository = hentSakerRepository;
		this.journalpostRepository = journalpostRepository;
		this.pdlIdentConsumer = pdlIdentConsumer;
	}

	@Transactional
	public String avsluttSaker(AvsluttSakRequest avsluttSakRequest) {
		List<Sak> saker = getSakerForRequest(avsluttSakRequest);

		List<Long> saksIds = saker.stream().map(Sak::getSakId).toList();
		var tilknyttedeJournalposter = journalpostRepository.fetchBySakIds(saksIds);

		if (tilknyttedeJournalposter.isEmpty()) {
			log.info("Fagsystemsaken har ingen tilknyttede journalposter. Avbryter tilhørende joark-saker.");
			avbrytSaker(saker);
			return AVBRUTT_STRING;
		}
		if (harSakenAapneJournalposterUnderRedigering(tilknyttedeJournalposter)) {
			throw new SakHarJournalposterUnderRedigeringException("Fagsystemsaken har en eller flere journalposter under redigering og kan ikke avsluttes.");
		}
		if (manglerSakenFerdigstilteJournalposter(tilknyttedeJournalposter)) {
			log.info("Fagsystemsaken har ingen ferdigstilte journalposter. Avbryter joark-saker.");
			avbrytSaker(saker);
			return AVBRUTT_STRING;
		}
		avsluttSaker(saker, avsluttSakRequest);
		return AVSLUTTET_STRING;
	}

	private void avbrytSaker(List<Sak> saker) {
		saker.forEach(sak -> {
			sak.setSakStatus(AVBRUTT);
			sak.setKassasjonStatus(KLAR_FOR_KASSASJON);
			sak.setAvleveringStatus(AvleveringStatusCode.AVBRUTT);
			sak.setDatoEndret(Date.from(LocalDateTime.now().atZone(ZONEID_NORGE).toInstant()));
			sak.setEndretAv(determineSaksbehandler());
		});
	}

	private void avsluttSaker(List<Sak> saker, AvsluttSakRequest avsluttSakRequest) {
		saker.forEach(sak -> {
			sak.setSakStatus(AVSLUTTET);
			sak.setAvleveringStatus(null);
			sak.setKassasjonStatus(null);
			sak.setEndretAv(MDC.get(MDC_USER_ID));
			sak.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			sak.setDatoEndret(Date.from(LocalDateTime.now().atZone(ZONEID_NORGE).toInstant()));
			sak.setDatoAvsluttet(determineDatoAvsluttet(avsluttSakRequest));
			sak.setAdministrativEnhet(avsluttSakRequest.getAdministrativEnhet());
			sak.setDatoSakOpprettet(convertLocalDateTimeToDate(avsluttSakRequest.getOpprettetDato()));
			sak.setSakAnsvarlig(determineSakAnsvarlig(avsluttSakRequest));
			sak.setAvsluttetAv(determineSaksbehandler());
			sak.setAvsluttetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		});
	}

	private String determineSaksbehandler() {
		String userId = MDC.get(MDC_USER_ID);
		return userId == null || userId.isBlank() ?
				MDC.get(MDC_CONSUMER_ID) : userId;
	}

	private Date determineDatoAvsluttet(AvsluttSakRequest avsluttSakRequest) {
		return avsluttSakRequest.getAvsluttetDato() == null ? Date.from(LocalDateTime.now().atZone(ZONEID_NORGE).toInstant()) :
				convertLocalDateTimeToDate(avsluttSakRequest.getAvsluttetDato());
	}

	private Date convertLocalDateTimeToDate(LocalDateTime ldt) {
		return Date.from(ldt.toInstant(systemDefault().getRules().getOffset(now())));
	}

	private String determineSakAnsvarlig(AvsluttSakRequest avsluttSakRequest) {
		String sakAnsvarlig = avsluttSakRequest.sakAnsvarlig;
		return sakAnsvarlig == null || sakAnsvarlig.isBlank() ?
				avsluttSakRequest.getAdministrativEnhet() :
				sakAnsvarlig;
	}

	private boolean manglerSakenFerdigstilteJournalposter(List<Journalpost> journalposts) {
		return journalposts.stream()
				.noneMatch(journalpost ->
						ferdigstilteJournalpostStatuser.contains(journalpost.getJournalstatus()));
	}

	private boolean harSakenAapneJournalposterUnderRedigering(List<Journalpost> journalposts) {
		return journalposts.stream()
				.anyMatch(journalpost ->
						midlertidigeJournalpostStatuser.contains(journalpost.getJournalstatus())
						&& !journalpost.isFeilregistrert());
	}

	private List<Sak> getSakerForRequest(AvsluttSakRequest avsluttSakRequest) {
		SakSearchCriteria criteria = generateSakSearchCriteria(avsluttSakRequest);
		var saker = hentSakerRepository.finnSaker(criteria);
		if (saker.isEmpty()) {
			throw new SakIkkeFunnetException(String.format("Fant ingen saker for fagsakID=%s og fagsaksystem=%s", avsluttSakRequest.getFagsakId(), avsluttSakRequest.getFagsaksystem()));
		}
		return saker;
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

	private SakSearchCriteria.SakSearchCriteriaBuilder generateBaseSakSearchCriteria(AvsluttSakRequest avsluttSakRequest) {
		return SakSearchCriteria.builder()
				.tema(singletonList(avsluttSakRequest.getTema()))
				.statuser(List.of(AAPEN))
				.soekNullStatus(true)
				.fagsakNr(avsluttSakRequest.fagsakId)
				.applikasjon(avsluttSakRequest.getFagsaksystem());
	}
}
