package no.nav.dokarkiv.core.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Abstrakt klasse for sporingsinfo under oppretting og endring av entitet.
 */
@SuppressWarnings("serial")
@MappedSuperclass
@Getter
@Setter
@ToString
public abstract class AbstractPersistentDomainObject implements Serializable {
	@Embedded
	private ChangeStamp changeStamp;
}