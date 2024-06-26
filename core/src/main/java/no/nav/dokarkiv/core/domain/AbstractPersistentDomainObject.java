package no.nav.dokarkiv.core.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
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
	protected ChangeStamp changeStamp;
}