package no.nav.dokarkiv.core.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Abstract base class for all persistent domain objects with version field.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractPersistentVersionedDomainObjectWithKilde extends AbstractPersistentVersionedDomainObject
		implements Identifiable {
	
	@Column(name = "opprettet_kilde_navn", nullable = false, updatable = false)
	private String opprettetKildeNavn;
	
	@Column(name = "endret_kilde_navn")
	private String endretKildeNavn;

	/**
	 * Checks if the Id is set.
	 * 
	 * @return true if Id is set, false otherwise.
	 */
	public boolean hasId() {
		return getId() != null;
	}
	
	/**
	 * Getter for the opprettetKildeNavn property.
	 *
	 * @return the opprettetKildeNavn
	 */
	public String getOpprettetKildeNavn() {
		return opprettetKildeNavn;
	}

	/**
	 * Setter for the opprettetKildeNavn property.
	 *
	 * @param opprettetKildeNavn the opprettetKildeNavn to set
	 */
	public void setOpprettetKildeNavn(String opprettetKildeNavn) {
		this.opprettetKildeNavn = opprettetKildeNavn;
	}

	/**
	 * Getter for the endretKildeNavn property.
	 *
	 * @return the endretKildeNavn
	 */
	public String getEndretKildeNavn() {
		return endretKildeNavn;
	}

	/**
	 * Setter for the endretKildeNavn property.
	 *
	 * @param endretKildeNavn the endretKildeNavn to set
	 */
	public void setEndretKildeNavn(String endretKildeNavn) {
		this.endretKildeNavn = endretKildeNavn;
	}

}