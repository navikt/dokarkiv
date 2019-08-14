package no.nav.dokarkiv.sak.repository;

import static org.apache.commons.lang3.StringUtils.defaultString;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.repository.SakRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Repository
public class HentSakerRepository {
	private static final Counter opprettedeSakerCounter = Counter.build("saker_opprettet_total", "Antall saker opprettet totalt")
			.labelNames("tema", "type", "applikasjon", "consumer").register();

	private static final Histogram latencyHisto = Histogram.build("repository_duration_seconds", "Repository latency in seconds")
			.labelNames("operation", "consumer")
			.register();

	private final EntityManager entityManager;
	private final SakRepository sakRepository;

	public HentSakerRepository(EntityManager entityManager, SakRepository sakRepository) {
		this.entityManager = entityManager;
		this.sakRepository = sakRepository;
	}

	public Sak lagre(Sak sak) {
		Histogram.Timer timer = startTimer("insert");
		try {
			sakRepository.save(sak);
		} finally {
			timer.observeDuration();
		}
		opprettedeSakerCounter.labels(
				sak.getTema(),
				sak.getFagsakNr() != null ? "Fagsak" : "Generell",
				defaultString(sak.getApplikasjon(), "N/A"),
				defaultString(MDC.get("consumerid"), "N/A")).inc();
		return sak;
	}

	public Optional<Sak> hentSak(Long id) {
		Histogram.Timer timer = startTimer("get");
		Optional<Sak> result;
		try {
			result = sakRepository.findById(id);
		} finally {
			timer.observeDuration();
		}
		return result;
	}

	public List<Sak> finnSaker(SakSearchCriteria sakSearchCriteria) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Sak> cq = cb.createQuery(Sak.class);
		Root<Sak> sak = cq.from(Sak.class);

		List<Predicate> predicates = new ArrayList<>();

		if (sakSearchCriteria.getAktoerId().isPresent()) {
			predicates.add(cb.equal(sak.get("aktoerId"), sakSearchCriteria.getAktoerId().get()));
		}

		if (sakSearchCriteria.getApplikasjon().isPresent()) {
			predicates.add(cb.equal(sak.get("applikasjon"), sakSearchCriteria.getApplikasjon().get()));
		}

		if (sakSearchCriteria.getOrgnr().isPresent()) {
			predicates.add(cb.equal(sak.get("orgnr"), sakSearchCriteria.getOrgnr().get()));
		}

		if (sakSearchCriteria.getFagsakNr().isPresent()) {
			predicates.add(cb.equal(sak.get("fagsakNr"), sakSearchCriteria.getFagsakNr().get()));
		}

		if (!sakSearchCriteria.getTema().isEmpty()) {
			predicates.add(cb.isTrue(sak.get("tema").in(sakSearchCriteria.getTema())));
		}

		cq.where(predicates.toArray(new Predicate[0]));
		cq.orderBy(cb.desc(sak.get("opprettetTidspunkt")));

		Histogram.Timer timer = startTimer("search");
		try {
			TypedQuery<Sak> query = entityManager.createQuery(cq);
			return query.getResultList();
		} finally {
			timer.observeDuration();
		}
	}

	private Histogram.Timer startTimer(String operation) {
		return latencyHisto
				.labels(
						operation,
						defaultString(MDC.get("consumerid"), "N/A")).startTimer();
	}
}
