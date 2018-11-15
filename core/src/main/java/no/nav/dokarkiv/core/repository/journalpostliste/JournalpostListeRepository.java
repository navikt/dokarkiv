package no.nav.dokarkiv.core.repository.journalpostliste;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
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
		// If empty saksliste, return empty list
		if (hentMinJPListeParameters.getSaksListe().isEmpty()) {
			return new ArrayList<>();
		}
		Session session = entityManager.unwrap(Session.class);
		JournalpostCriterionBuilder criterionBuilder = new JournalpostCriterionBuilder(session);

		Criteria criteria = criterionBuilder.buildCriteria(hentMinJPListeParameters);

		if (hentMinJPListeParameters.getMaxResults() > 0) {
			criteria.setMaxResults((int) hentMinJPListeParameters.getMaxResults());
		}

		if (hentMinJPListeParameters.getMaxResults() > 0 && hentMinJPListeParameters.getPageNr() > 0) {
			criteria.setFirstResult((int) (hentMinJPListeParameters.getMaxResults() * hentMinJPListeParameters.getPageNr()));
		}

		return (List<Journalpost>) criteria.list();
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
