package no.nav.dokarkiv.core.repository;

import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Utdrag fra HibernateRepositoryImpl https://github.com/vladmihalcea/hibernate-types
 *
 * @param <T> JPA entiteten.
 */
public class HibernateRepositoryImpl<T> implements HibernateRepository<T> {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public <S extends T> S persist(S entity) {
		entityManager.persist(entity);
		return entity;
	}

	@Override
	public <S extends T> S persistAndFlush(S entity) {
		persist(entity);
		entityManager.flush();
		return entity;
	}

	@Override
	public <S extends T> List<S> persistAll(Iterable<S> entities) {
		List<S> result = new ArrayList<>();
		for(S entity : entities) {
			result.add(persist(entity));
		}
		return result;
	}

	@Override
	public <S extends T> S merge(S entity) {
		return entityManager.merge(entity);
	}

	@Override
	public <S extends T> S mergeAndFlush(S entity) {
		S result = merge(entity);
		entityManager.flush();
		return result;
	}

	@Override
	public <S extends T> S update(S entity) {
		session().update(entity);
		return entity;
	}

	@Override
	public <S extends T> S updateAndFlush(S entity) {
		update(entity);
		entityManager.flush();
		return entity;
	}


	protected Session session() {
		return entityManager.unwrap(Session.class);
	}
}
