package no.nav.dokarkiv.innsynjournal.v2.tjoark053.repository;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class Tjoark053JournalpostListeRepository {

	private final EntityManager entityManager;

	@Inject
	public Tjoark053JournalpostListeRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@SuppressWarnings("unchecked")
	public List<Journalpost> findJournalpostListe(HentMinJPListeParameters hentMinJPListeParameters) {
		List<Journalpost> foundJournalposts = Lists.newArrayList();
		// If empty saksliste, return empty list
		if (hentMinJPListeParameters.getSaksListe().isEmpty()) {
			return foundJournalposts;
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
		foundJournalposts = criteria.list();
		return foundJournalposts;
	}

}
