package no.nav.dokarkiv.journalpost.v1.api.avsluttSak;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.pdl.PdlIdentConsumer;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.SakStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.SakIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_APP_ID;
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
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AAPEN;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;

@Slf4j
@Component
public class AvsluttSakService {

	private final HentSakerRepository hentSakerRepository;
	private final JournalpostRepository journalpostRepository;
	private final SakRepository sakRepository;
	private final PdlIdentConsumer pdlIdentConsumer;
	private final Set<JournalStatusCode> midlertidigeJournalpostStatuser = Set.of(R, D, M, MO, OD);
	private final Set<JournalStatusCode> ferdigstilteJournalpostStatuser = Set.of(FL, FS, E, J);;

	private final String KAN_KASSERES = "KAN_KASSERES";

	public AvsluttSakService(HentSakerRepository hentSakerRepository, JournalpostRepository journalpostRepository, SakRepository sakRepository, PdlIdentConsumer pdlIdentConsumer) {
		this.hentSakerRepository = hentSakerRepository;
		this.journalpostRepository = journalpostRepository;
		this.sakRepository = sakRepository;
		this.pdlIdentConsumer = pdlIdentConsumer;
	}

	@Transactional
	public void avsluttSak(AvsluttSakRequest avsluttSakRequest) {
		List<Sak> saker = getSakerForRequest(avsluttSakRequest);

		List<Long> saksIds = saker.stream().map(Sak::getSakId).collect(Collectors.toList());
		var journalposts = journalpostRepository.fetchBySakIds(saksIds);

		if (!journalposts.isEmpty()) {
			if(harSakenJournalposterUnderRedigering(journalposts)){
				//TODO: actual feilmelding
				throw new DokarkivFunctionalException("dum journalpost :( Den kan ikke avsluttes :(");
			}
			if (harSakenFerdigstilteJournalposter(journalposts)) {
				tre_en_avbrytSak(saker);
				log.info("3.1 avbryt sak");
			}
		}

		fire_null_avsluttSak(saker);
	}

	private void tre_en_avbrytSak(List<Sak> saks) {
		saks.forEach(sak -> {
			sak.setSakStatus(SakStatusCode.AVBRUTT);
			sak.setKassasjonStatus(KAN_KASSERES);
			sak.setAvleveringStatus("AVBRUTT");
			sak.setDatoEndret(DateTime.now().toDate());
			sak.setEndretAv(MDC.get(MDC_USER_ID));  //TODO: Blir dette riktig?
		});
	}

	private void fire_null_avsluttSak(List<Sak> saker) {
		saker.forEach(sak -> {
			sak.setSakStatus(SakStatusCode.AVSLUTTET);
			sak.setKassasjonStatus(null);
			sak.setAvleveringStatus(null);
			sak.setEndretAv(MDC.get(MDC_USER_ID)); // TODO: Blir dette riktig?
			sak.setEndretAvKildeNavn(MDC.get(MDC_APP_ID));
			sak.setDatoEndret(DateTime.now().toDate());
			sak.setAdministrativEnhet(sak.getAdministrativEnhet());
			sak.setDatoAvsluttet(sak.getDatoAvsluttet());
			sak.setSakAnsvarlig(determineSakAnsvarlig(sak));
		});
	}

	private String determineSakAnsvarlig(Sak sak){
		return sak.getSakAnsvarlig() == null || sak.getSakAnsvarlig().isBlank() ? sak.getAdministrativEnhet() : sak.getSakAnsvarlig();
	}

	private boolean harSakenFerdigstilteJournalposter(List<Journalpost> journalposts) {
		return journalposts.stream()
				.anyMatch(journalpost -> ferdigstilteJournalpostStatuser.contains(journalpost.getJournalstatus()));
	}

	private boolean harSakenJournalposterUnderRedigering(List<Journalpost> journalposts){
		return journalposts.stream()
				.anyMatch(journalpost -> midlertidigeJournalpostStatuser.contains(journalpost.getJournalstatus()));
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
