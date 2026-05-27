package no.nav.dokarkiv.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstrakt klasse som alle JPA entiteter arver fra.
 */
@SuppressWarnings("serial")
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractPersistentVersionedDomainObjectWithKilde extends AbstractPersistentVersionedDomainObject
		implements Identifiable {
	public static final int KILDE_NAVN_LENGTH = 40;

	@Column(name = "opprettet_kilde_navn", nullable = false, updatable = false, length = KILDE_NAVN_LENGTH)
	private String opprettetKildeNavn;

	@Column(name = "endret_kilde_navn", length = KILDE_NAVN_LENGTH)
	private String endretKildeNavn;

	/**
	 * Checks if the Id is set.
	 *
	 * @return true if Id is set, false otherwise.
	 */
	public boolean hasId() {
		return getId() != null;
	}
}