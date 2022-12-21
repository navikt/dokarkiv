package no.nav.dokarkiv.core.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * Base repository til bruk i test skopet.
 * <p>
 * Inneholder operasjoner man ikke burde bruke i produksjon.
 * Kan utvides med flere spring data jpa metoder.
 *
 * @param <T>  JPA entiteten
 * @param <ID> ID datatypen
 */
@NoRepositoryBean
public interface BaseJpaTestRepository<T, ID> extends BaseJpaRepository<T, ID> {
	/**
	 * Returner alle entiteter av denne typen.
	 *
	 * @return alle entiteter
	 */
	List<T> findAll();

	/**
	 * Sletter alle entiteter dette repository behandler.
	 */
	void deleteAll();
}
