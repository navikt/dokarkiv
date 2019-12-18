package no.nav.dokarkiv.core.repository.journalpostliste;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import org.joda.time.DateTime;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
@Transactional
public class JournalpostListeRepository {

	private final EntityManager entityManager;

	@Inject
	public JournalpostListeRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@SuppressWarnings("unchecked")
	public List<Journalpost> findJournalpostListe(HentMinJPListeParameters hentMinJPListeParameters) {
		List<Journalpost> foundJournalposts = new ArrayList<>();

		// If empty saksliste, return empty list
		if (hentMinJPListeParameters.getSaksListe().isEmpty()) {
			return new ArrayList<>();
		}
		Session session = entityManager.unwrap(Session.class);
		JournalpostCriterionBuilder criterionBuilder = new JournalpostCriterionBuilder(session);

		Criteria criteria = criterionBuilder.buildCriteria(hentMinJPListeParameters);

		if (hentMinJPListeParameters.getMaxResults() > 0 && hentMinJPListeParameters.getPageNr() == 0) {
			int maxResult = Math.min(criteria.list().size(), (int) hentMinJPListeParameters.getMaxResults());
			foundJournalposts = criteria.list().subList(0, maxResult);
		}

		if (hentMinJPListeParameters.getMaxResults() > 0 && hentMinJPListeParameters.getPageNr() > 0) {
			int firstResult = (int) (hentMinJPListeParameters.getMaxResults() * hentMinJPListeParameters.getPageNr());
			if (firstResult > criteria.list().size()) {
				return new ArrayList<>();
			}
			int maxResult = Math.min(criteria.list().size(), firstResult + (int) hentMinJPListeParameters.getMaxResults());
			foundJournalposts = criteria.list().subList(firstResult, maxResult);
		}

		if (foundJournalposts.isEmpty()) {
			foundJournalposts = (List<Journalpost>) criteria.list();
		}

		return foundJournalposts;
	}

	public long findTotalNumberOfJournalposts(HentMinJPListeParameters hentMinJPListeParameters) {
		if (hentMinJPListeParameters.getSaksListe().isEmpty()) {
			return 0;
		}
		Session session = entityManager.unwrap(Session.class);
		JournalpostCriterionBuilder criterionBuilder = new JournalpostCriterionBuilder(session);

		Criteria criteria = criterionBuilder.buildCriteria(hentMinJPListeParameters);
		addCountToCriteria(criteria);
		return (Long) criteria.uniqueResult();
	}

	private void addCountToCriteria(Criteria criteria) {
		criteria.setProjection(Projections.rowCount());
	}
}
