package no.nav.dokarkiv.core.repository;

import java.util.List;

/**
 * Inspirert fra HibernateRepository https://github.com/vladmihalcea/hibernate-types
 * <p>
 * Metoder for å behandle Hibernate sine entity state changes.
 *
 * @param <T> JPA entiteten.
 */
public interface HibernateRepository<T> {
	/**
	 * Persisterer entitet og gjør den managed.
	 * <p>
	 * Bruk denne metoden hvis du skal lagre et nytt objekt.
	 *
	 * @param entity som skal persisteres
	 * @param <S>    entitetstype
	 * @return managed entity
	 */
	<S extends T> S persist(S entity);

	/**
	 * Persisterer entitet og gjør den managed.
	 * <p>
	 * Bruk denne metoden hvis du skal lagre et nytt objekt og synkronisere det med databasen.
	 * Denne metoden committer ikke endringene. Det er det transaksjonen som gjør!
	 *
	 * @param entity som skal persisteres
	 * @param <S>    entitetstype
	 * @return managed entity
	 */
	<S extends T> S persistAndFlush(S entity);

	/**
	 * Persisterer flere entiteter og gjør de managed.
	 * <p>
	 * Bruk denne metoden hvis du skal lagre flere enn ett nytt objekt.
	 *
	 * @param entities som skal persisteres
	 * @param <S>    entitetstype
	 * @return liste av managed entiteter
	 */
	<S extends T> List<S> persistAll(Iterable<S> entities);

	/**
	 * Kopierer tilstanden fra detached entitet til entitet som er managed.
	 * <p>
	 * Bruk denne metoden hvis du synkronisere endringer til databasen fra en detached entitet, med dirty checking.
	 * Gjør en select før update.
	 *
	 * @param entity som skal persisteres
	 * @param <S>    entitetstype
	 * @return managed entitet
	 */
	<S extends T> S merge(S entity);

	/**
	 * Kopierer tilstanden fra detached entitet til entitet som er managed.
	 * <p>
	 * Bruk denne metoden hvis du synkronisere endringer til databasen fra en detached entitet, med dirty checking.
	 * Gjør en select før update.
	 * Denne metoden committer ikke endringene. Det er det transaksjonen som gjør!
	 *
	 * @param entity som skal persisteres
	 * @param <S>    entitetstype
	 * @return managed entitet
	 */
	<S extends T> S mergeAndFlush(S entity);

	/**
	 * Tvinger kopi av tilstanden fra detached entitet til entitet som er managed.
	 * <p>
	 * Bruk denne metoden hvis du vil tvinge endringer til databasen fra en detached entitet, uten dirty checking.
	 * Gjør en select før update.
	 *
	 * @param entity som skal persisteres
	 * @param <S>    entitetstype
	 * @return managed entitet
	 */
	<S extends T> S update(S entity);

	/**
	 * Tvinger kopi av tilstanden fra detached entitet til entitet som er managed.
	 * <p>
	 * Bruk denne metoden hvis du vil tvinge endringer til databasen fra en detached entitet, uten dirty checking.
	 * Gjør en select før update.
	 * Denne metoden committer ikke endringene. Det er det transaksjonen som gjør!
	 *
	 * @param entity som skal persisteres
	 * @param <S>    entitetstype
	 * @return managed entitet
	 */
	<S extends T> S updateAndFlush(S entity);
}
