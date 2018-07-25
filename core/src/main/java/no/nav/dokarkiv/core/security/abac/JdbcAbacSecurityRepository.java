package no.nav.dokarkiv.core.security.abac;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Implementation of AbacSecurityRepository
 *
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
@Repository
public class JdbcAbacSecurityRepository implements AbacSecurityRepository {

	private static final Logger LOG = LoggerFactory.getLogger(JdbcAbacSecurityRepository.class);

	private static final String FINN_SAKSRELASJON_PAA_JOURNALPOST = "select sak_nr_fk, k_fagsystem from T_SAKSRELASJON where journalpost_id = :journalpostId";
	private static final String FINN_BRUKERE_PAA_JOURNALPOST = "select cast(bruker_id as varchar(11)) from T_BRUKER where journalpost_id = :journalpostId";
	private static final String FINN_FAGOMRADE_PAA_JOURNALPOST = "select K_FAGOMRADE from T_JOURNALPOST where journalpost_id = :journalpostId";

	@Inject
	private EntityManager entityManager;

	private Query getQuery(String query, Long journalpostId) {
		return entityManager.unwrap(Session.class).createSQLQuery(query).setParameter("journalpostId", journalpostId);
	}

	@Override
	public AbacResources findAbacResources(Long journalpostId) {
		AbacResources result = new AbacResources();
		try {
			Object[] saksrelasjon = findSaksrelasjoner(journalpostId);
			if (saksrelasjon != null) {
				if (saksrelasjon.length > 0 && saksrelasjon[0] instanceof String) {
					result.setSakId((String) saksrelasjon[0]);
				}
				if (saksrelasjon.length > 1 && saksrelasjon[1] instanceof String) {
					result.setFagsystem(FagsystemCode.valueOf((String) saksrelasjon[1]));
				}
			}

			List<String> brukerIds = findBrukere(journalpostId);
			if (brukerIds != null) {
				result.setBrukerIds(brukerIds);
			}

			String fagomrade = findFagomrade(journalpostId);
			if (StringUtils.isNotEmpty(fagomrade)) {
				result.setFagomrade(FagomradeCode.valueOf(fagomrade));
			}
		} catch (HibernateException e) {
			LOG.warn("Could not find AbacResources for journalpostId=" + journalpostId, e);
			return result;
		}
		return result;
	}

	private List<String> findBrukere(Long journalpostId) {
		Query q = getQuery(FINN_BRUKERE_PAA_JOURNALPOST, journalpostId);
		return (List<String>) q.list();
	}

	private Object[] findSaksrelasjoner(Long journalpostId) {
		Query q = getQuery(FINN_SAKSRELASJON_PAA_JOURNALPOST, journalpostId);
		return (Object[]) q.uniqueResult();
	}

	private String findFagomrade(Long journalpostId) {
		Query q = getQuery(FINN_FAGOMRADE_PAA_JOURNALPOST, journalpostId);
		Object o = q.uniqueResult();
		if (o instanceof String) {
			return (String) o;
		}
		return null;
	}
}
