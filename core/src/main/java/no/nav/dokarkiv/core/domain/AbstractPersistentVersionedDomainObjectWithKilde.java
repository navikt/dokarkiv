package no.nav.dokarkiv.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.slf4j.MDC;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

/**
 * Abstrakt klasse som alle JPA entiteter arver fra.
 */
@SuppressWarnings("serial")
@Getter
@MappedSuperclass
public abstract class AbstractPersistentVersionedDomainObjectWithKilde extends AbstractPersistentVersionedDomainObject
		implements Identifiable {
	public static final int KILDE_NAVN_LENGTH = 40;

	@Column(name = "opprettet_kilde_navn", nullable = false, updatable = false, length = KILDE_NAVN_LENGTH)
	private String opprettetKildeNavn;

	@Column(name = "endret_kilde_navn", length = KILDE_NAVN_LENGTH)
	private String endretKildeNavn;

	@PrePersist
	void setOpprettetKildeNavnPrePersist() {
		if (opprettetKildeNavn == null) {
			opprettetKildeNavn = resolveKildeNavn();
		}
	}

	@PreUpdate
	void setEndretKildeNavnPreUpdate() {
		endretKildeNavn = resolveKildeNavn();
	}

	private static String resolveKildeNavn() {
		String kildeNavn = MDC.get(MDC_CONSUMER_ID);
		if (kildeNavn == null && RequestContextHolder.isRequestContextSet()) {
			kildeNavn = RequestContextHolder.currentRequestContext().getComponentId();
		}
		if (kildeNavn == null) {
			kildeNavn = "DEFAULT_KILDE_NAVN";
		}
		if (kildeNavn.length() > KILDE_NAVN_LENGTH) {
			kildeNavn = kildeNavn.substring(0, KILDE_NAVN_LENGTH - 1);
		}
		return kildeNavn;
	}

	/**
	 * Checks if the Id is set.
	 *
	 * @return true if Id is set, false otherwise.
	 */
	public boolean hasId() {
		return getId() != null;
	}
}
