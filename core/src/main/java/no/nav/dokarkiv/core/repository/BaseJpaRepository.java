package no.nav.dokarkiv.core.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

/**
 * Base JPA repository interface.
 * <p>
 * Inneholder metoder som er tilgjengelig for utviklere i produksjonskode.
 * Kan utvides med flere metoder som burde være felles for entitet repositories.
 *
 * @param <T>  JPA entiteten
 * @param <ID> ID datatypen
 */
@NoRepositoryBean
public interface BaseJpaRepository<T, ID> extends Repository<T, ID> {
	/**
	 * Henter entitet basert på id.
	 * <p>
	 * Bruk denne hvis du behøver tilgang til feltene på entiteten.
	 *
	 * @param id kan ikke være {@literal null}.
	 * @return Entitet med id eller {@literal Optional#empty()} hvis den ikke finnes.
	 * @throws IllegalArgumentException hvis {@literal id} er {@literal null}.
	 */
	Optional<T> findById(ID id);

	/**
	 * Returnerer en referanse (proxy) til entiteten med id.
	 * <p>
	 * Bruk denne hvis du har id til entiteten du f.eks skal knytte andre entiteter til.
	 * <p>
	 * Les dette innlegget for riktig bruk <a href="https://vladmihalcea.com/spring-data-jpa-findbyid/">spring-data-jpa-findbyid</a>
	 *
	 * @param id kan ikke være {@literal null}.
	 * @return En referanse (proxy) til entiteten med id.
	 * @see EntityManager#getReference(Class, Object) detaljer på exceptions.
	 */
	T getReferenceById(ID id);

	/**
	 * Finner ut om en entitet med id finnes.
	 *
	 * @param id kan ikke være {@literal null}.
	 * @return {@literal true} hvis en entitet med id finnes, {@literal false} ellers.
	 * @throws IllegalArgumentException hvis {@literal id} er {@literal null}.
	 */
	boolean existsById(ID id);
}
