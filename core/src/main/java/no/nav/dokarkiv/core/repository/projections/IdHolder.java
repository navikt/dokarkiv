package no.nav.dokarkiv.core.repository.projections;

/**
 * Brukes for å returnere Spring Data JPA DTO projections. Der id er det eneste feltet.
 *
 * @param id
 */
public record IdHolder(Long id) {
}
