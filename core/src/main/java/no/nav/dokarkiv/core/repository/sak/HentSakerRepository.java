package no.nav.dokarkiv.core.repository.sak;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.repository.SakRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Repository
public class HentSakerRepository {

	private final EntityManager entityManager;
	private final SakRepository sakRepository;
	private MeterRegistry meterRegistry;

	public HentSakerRepository(EntityManager entityManager, SakRepository sakRepository,
							   MeterRegistry meterRegistry) {
		this.entityManager = entityManager;
		this.sakRepository = sakRepository;
		this.meterRegistry = meterRegistry;
	}

	public Sak lagre(Sak sak) {
		sakRepository.persist(sak);
		initSakerRepoCounter(meterRegistry, sak.getTema(), sak.getApplikasjon(), sak.getFagsakNr()).increment();
		return sak;
	}

	public Optional<Sak> hentSak(Long id) {
		return sakRepository.findById(id);
	}

	public List<Sak> finnSaker(SakSearchCriteria sakSearchCriteria) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Sak> cq = cb.createQuery(Sak.class);
		Root<Sak> sak = cq.from(Sak.class);

		List<Predicate> predicates = new ArrayList<>();

		if (!sakSearchCriteria.getAktoerId().isEmpty() && !isBlank(sakSearchCriteria.getAktoerId().get(0))) {
			predicates.add(cb.isTrue(sak.get("aktoerId").in(sakSearchCriteria.getAktoerId())));
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

		if (!sakSearchCriteria.getStatuser().isEmpty()) {
			if (sakSearchCriteria.getSoekNullStatus().isPresent() && sakSearchCriteria.getSoekNullStatus().get()) {
				predicates.add(cb.or(cb.isTrue(sak.get("sakStatus").in(sakSearchCriteria.getStatuser())), cb.isTrue(sak.get("sakStatus").isNull())));
			} else {
				predicates.add(cb.or(cb.isTrue(sak.get("sakStatus").in(sakSearchCriteria.getStatuser()))));
			}
		}

		cq.where(predicates.toArray(new Predicate[0]));
		cq.orderBy(cb.asc(sak.get("sakId")));

		TypedQuery<Sak> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	public List<Sak> finnSakerForGjenaapneSak(SakSearchCriteria sakSearchCriteria) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Sak> cq = cb.createQuery(Sak.class);
		Root<Sak> sak = cq.from(Sak.class);

		List<Predicate> predicates = new ArrayList<>();

		predicates.add(cb.isTrue(sak.get("tema").in(sakSearchCriteria.getTema())));
		predicates.add(cb.isTrue(sak.get("sakStatus").in("AVSLUTTET")));
		predicates.add(cb.isTrue(sak.get("avleveringStatus").isNull()));
		predicates.add(cb.isTrue(sak.get("kassasjonStatus").isNull()));
		predicates.add(cb.equal(sak.get("fagsakNr"), sakSearchCriteria.getFagsakNr().get()));
		predicates.add(cb.equal(sak.get("applikasjon"), sakSearchCriteria.getApplikasjon().get()));

		if (!sakSearchCriteria.getAktoerId().isEmpty() && !isBlank(sakSearchCriteria.getAktoerId().get(0))) {
			predicates.add(cb.isTrue(sak.get("aktoerId").in(sakSearchCriteria.getAktoerId())));
		} else {
			predicates.add(cb.equal(sak.get("orgnr"), sakSearchCriteria.getOrgnr().get()));
		}
		cq.where(predicates.toArray(new Predicate[0]));
		cq.orderBy(cb.asc(sak.get("sakId")));

		TypedQuery<Sak> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	public static Counter initSakerRepoCounter(MeterRegistry meterRegistry, String tema, String applikasjon, String fagsakNr) {
		return Counter.builder("repository_duration_seconds")
				.tag("tema", tema == null ? "ukjent" : tema)
				.tag("applikasjon", applikasjon == null ? "ukjent" : applikasjon)
				.tag("fagsakNr", fagsakNr == null ? "ukjent" : fagsakNr)
				.register(meterRegistry);
	}


}
