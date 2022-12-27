package no.nav.dokarkiv.core.repository;

import java.util.List;

/**
 * Utdrag fra HibernateRepository https://github.com/vladmihalcea/hibernate-types
 * <p>
 * Metoder for å behandle Hibernate sine entity state changes.
 * @param <T> JPA entiteten.
 */
public interface HibernateRepository<T> {
	<S extends T> S persist(S entity);

	<S extends T> S persistAndFlush(S entity);

	<S extends T> List<S> persistAll(Iterable<S> entities);

	<S extends T> S merge(S entity);

	<S extends T> S mergeAndFlush(S entity);

	<S extends T> S update(S entity);

	<S extends T> S updateAndFlush(S entity);
}
