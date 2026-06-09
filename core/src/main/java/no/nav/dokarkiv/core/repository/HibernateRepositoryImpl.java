package no.nav.dokarkiv.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
}
