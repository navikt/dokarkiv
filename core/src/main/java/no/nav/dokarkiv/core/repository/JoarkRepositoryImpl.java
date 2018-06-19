package no.nav.dokarkiv.core.repository;

import org.hibernate.NonUniqueResultException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JoarkRepositoryImpl implements JoarkCustomRepository {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Long findJournalpostIdByTillegssopplysningKeyAndValue(String nokkel, String verdi) {
		if (nokkel == null || verdi == null) {
			return null;
		}
		Query q = em.createQuery("select jt.journalpost_id from t_jp_tillegg jt where jt.nokkel = :nokkel and jt.verdi = :verdi");
		q.setParameter("nokkel", nokkel);
		q.setParameter("verdi", verdi);

		Long journalpostId;
		try {
			journalpostId = (Long) q.getSingleResult();
		} catch (NonUniqueResultException e) {
			return null;
		}
		return journalpostId;
	}
}
