package no.nav.dokarkiv.journalpost.v1.api.avsluttSak;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.pdl.PdlIdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AvleveringStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.SakHarJournalposterUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.SakIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
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
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;

@Slf4j
@Component
public class AvsluttSakService {

	private final HentSakerRepository hentSakerRepository;
	private final JournalpostRepository journalpostRepository;
	private final PdlIdentConsumer pdlIdentConsumer;
	private final Set<JournalStatusCode> midlertidigeJournalpostStatuser = Set.of(R, D, M, MO, OD);
	private final Set<JournalStatusCode> ferdigstilteJournalpostStatuser = Set.of(FL, FS, E, J);
	private final SakRepository sakRepository;

	public AvsluttSakService(HentSakerRepository hentSakerRepository, JournalpostRepository journalpostRepository, PdlIdentConsumer pdlIdentConsumer, SakRepository sakRepository) {
		this.hentSakerRepository = hentSakerRepository;
		this.journalpostRepository = journalpostRepository;
		this.pdlIdentConsumer = pdlIdentConsumer;
		this.sakRepository = sakRepository;
	}

	@Transactional
	public void avsluttSaker(AvsluttSakRequest avsluttSakRequest) {
		List<Sak> saker = getSakerForRequest(avsluttSakRequest);

		List<Long> saksIds = saker.stream().map(Sak::getSakId).collect(Collectors.toList());
		var journalposts = journalpostRepository.fetchBySakIds(saksIds);

		if (journalposts.isEmpty()) {
			log.info("Saken har ingen tilknyttede journalposter. Avbryter sak.");
			avbrytSaker(saker);
			return;
		}
		if (harSakenApneJournalposterUnderRedigering(journalposts)) {
			throw new SakHarJournalposterUnderRedigeringException("Saken har en eller flere journalposter under redigering og kan ikke avsluttes.");
		}
		if (!harSakenFerdigstilteJournalposter(journalposts)) {
			log.info("Saken har ingen ferdigstilte journalposter. Avbryter sak.");
			avbrytSaker(saker);
			return;
		}
		avsluttSaker(saker, avsluttSakRequest);
		log.info("AvsluttSakService har avsluttet sak");


	}

	private void avbrytSaker(List<Sak> saker) {
		saker.forEach(sak -> {
			sak.setSakStatus(AVBRUTT);
			sak.setKassasjonStatus(KLAR_FOR_KASSASJON);
			sak.setAvleveringStatus(AvleveringStatusCode.AVBRUTT);
			sak.setDatoEndret(DateTime.now().toDate());
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
			sak.setDatoEndret(DateTime.now().toDate());
			sak.setDatoAvsluttet(determineDatoAvsluttet(avsluttSakRequest));
			sak.setAdministrativEnhet(avsluttSakRequest.getAdministrativEnhet());
			sak.setDatoSakOpprettet(convertLocalDateTimeToDate(avsluttSakRequest.getOpprettetDato()));
			sak.setSakAnsvarlig(determineSakAnsvarlig(avsluttSakRequest));
			sak.setAvsluttetAv(determineSaksbehandler());
			sak.setAvsluttetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		});
	}

	private String determineSaksbehandler(){
		String userId = MDC.get(MDC_USER_ID);
		return userId == null || userId.isBlank() ?
				MDC.get(MDC_CONSUMER_ID) : userId;
	}

	private Date determineDatoAvsluttet(AvsluttSakRequest avsluttSakRequest) {
		return avsluttSakRequest.getAvsluttetDato() == null ? DateTime.now().toDate() :
				convertLocalDateTimeToDate(avsluttSakRequest.getAvsluttetDato());
	}

	private Date convertLocalDateTimeToDate(LocalDateTime ldt){
		return Date.from(ldt.toInstant(systemDefault().getRules().getOffset(now())));
	}

	private String determineSakAnsvarlig(AvsluttSakRequest avsluttSakRequest) {
		String sakAnsvarlig = avsluttSakRequest.sakAnsvarlig;
		return sakAnsvarlig == null || sakAnsvarlig.isBlank() ?
				avsluttSakRequest.getAdministrativEnhet() :
				sakAnsvarlig;
	}

	private boolean harSakenFerdigstilteJournalposter(List<Journalpost> journalposts) {
		return journalposts.stream()
				.anyMatch(journalpost -> ferdigstilteJournalpostStatuser.contains(journalpost.getJournalstatus()));
	}

	private boolean harSakenApneJournalposterUnderRedigering(List<Journalpost> journalposts) {
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
		if (ORGNR.equals(avsluttSakRequest.getBruker().getIdType())) {
			return generateOrganisasjonSakCriteria(avsluttSakRequest);
		} else {
			var aktoerIds = pdlIdentConsumer.hentHistoriskeAktoerIds(avsluttSakRequest.getBruker().getId());
			return generateAktoerIdCriteria(avsluttSakRequest, aktoerIds);
		}
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
				.fagsakNr(avsluttSakRequest.fagsakId)
				.applikasjon(avsluttSakRequest.getFagsaksystem());
	}
}
