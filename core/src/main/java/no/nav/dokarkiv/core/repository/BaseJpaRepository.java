package no.nav.dokarkiv.core.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Base JPA repository interface
 *
 * @param <T> JPA entiteten
 * @param <ID> ID datatypen
 */
@NoRepositoryBean
public interface BaseJpaRepository<T, ID> extends Repository<T, ID> {
	Optional<T> findById(ID id);

	T getReferenceById(ID id);

	boolean existsById(ID id);
}
