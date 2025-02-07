package no.nav.dokarkiv.journalpost.v1.api.sak;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.pdl.PdlIdentConsumer;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.SakIkkeFunnetException;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AAPEN;

@Slf4j
@Service
public class GjenaapneSakService {

	private final HentSakerRepository hentSakerRepository;
	private final PdlIdentConsumer pdlIdentConsumer;

	public GjenaapneSakService(HentSakerRepository hentSakerRepository, PdlIdentConsumer pdlIdentConsumer) {
		this.hentSakerRepository = hentSakerRepository;
		this.pdlIdentConsumer = pdlIdentConsumer;
	}

	@Transactional
	public void gjenaapneFagsak(GjenaapneSakRequest gjenaapneSakRequest) {
		List<Sak> saker = finnSakerSomSkalGjenaapnes(gjenaapneSakRequest);
		log.info("GjenaapneSak fant {} tilhørende sak-innslag som skal gjenåpnes med sakIds={} for fagsak med fagsakId={}",
				saker.size(), saker.stream().map(Sak::getSakId).toList(), gjenaapneSakRequest.fagsakId);

		gjenaapneSaker(saker);
	}

	private void gjenaapneSaker(List<Sak> saker) {
		saker.forEach(sak -> {
			sak.setSakStatus(AAPEN);
			sak.setEndretAv(MDC.get(MDC_USER_ID));
			sak.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			sak.setDatoEndret(Date.from(LocalDateTime.now().atZone(ZoneId.of("Europe/Oslo")).toInstant()));
			sak.setDatoAvsluttet(null);
			sak.setAvsluttetAv(null);
			sak.setAvsluttetKildeNavn(null);
		});
	}

	private List<Sak> finnSakerSomSkalGjenaapnes(GjenaapneSakRequest gjenaapneSakRequest) {
		SakSearchCriteria criteria = generateSakSearchCriteria(gjenaapneSakRequest);
		var saker = hentSakerRepository.finnSakerForGjenaapneSak(criteria);
		if (saker.isEmpty()) {
			throw new SakIkkeFunnetException(String.format("Fant ingen arkivsak for fagsakId=%s og fagsaksystem=%s", gjenaapneSakRequest.getFagsakId(), gjenaapneSakRequest.getFagsaksystem()));
		}
		return saker;
	}

	private SakSearchCriteria generateSakSearchCriteria(GjenaapneSakRequest gjenaapneSakRequest) {
		return switch (gjenaapneSakRequest.getBruker().getIdType()) {
			case ORGNR -> generateOrganisasjonSakCriteria(gjenaapneSakRequest);
			case AKTOERID, FNR -> {
				var aktoerIds = pdlIdentConsumer.hentHistoriskeAktoerIdsForAktoerId(gjenaapneSakRequest.getBruker().getId());
				yield generateAktoerIdSakCriteria(gjenaapneSakRequest, aktoerIds);
			}
		};
	}

	private SakSearchCriteria generateAktoerIdSakCriteria(GjenaapneSakRequest gjenaapneSakRequest, List<String> aktoerIds) {
		return generateBaseSakSearchCriteria(gjenaapneSakRequest)
				.aktoerId(aktoerIds)
				.build();
	}

	private SakSearchCriteria generateOrganisasjonSakCriteria(GjenaapneSakRequest gjenaapneSakRequest) {
		return generateBaseSakSearchCriteria(gjenaapneSakRequest)
				.orgnr(gjenaapneSakRequest.getBruker().getId())
				.build();
	}

	private SakSearchCriteria.SakSearchCriteriaBuilder generateBaseSakSearchCriteria(GjenaapneSakRequest gjenaapneSakRequest) {
		return SakSearchCriteria.builder()
				.tema(singletonList(gjenaapneSakRequest.getTema()))
				.fagsakNr(gjenaapneSakRequest.fagsakId)
				.applikasjon(gjenaapneSakRequest.getFagsaksystem());
	}
}
