package no.nav.dokarkiv.core.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Abstrakt klasse for sporingsinfo under oppretting og endring av entitet.
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractPersistentDomainObject implements Serializable {

	/**
	 * All persistent classes need a change stamp
	 */
	@Embedded
	private ChangeStamp changeStamp;

	/**
	 * Returns the change stamp
	 *
	 * @return The change stamp
	 */
	public ChangeStamp getChangeStamp() {
		return changeStamp;
	}

	/**
	 * Sets the change stamp.
	 *
	 * @param changeStamp The change stamp to set
	 */
	public void setChangeStamp(ChangeStamp changeStamp) {
		this.changeStamp = changeStamp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}