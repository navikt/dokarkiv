package no.nav.dokarkiv.core.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Abstract base class for all persistent domain objects.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Magnus Skuland, Sirius IT
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractPersistentDomainObject implements Serializable {

	/**
	 * All persistent classes need a change stamp:
	 */
	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "createdBy",
					column = @Column(name = "opprettet_av", insertable = true, updatable = false, nullable=false)),
			@AttributeOverride(name = "createdDate",
					column = @Column(name = "dato_opprettet", insertable = true, updatable = false, nullable = false)),
			@AttributeOverride(name = "updatedBy", column = @Column(name = "endret_av")),
			@AttributeOverride(name = "updatedDate", column = @Column(name = "dato_endret")) })
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
	 * @param changeStamp
	 *            The change stamp to set
	 */
	public void setChangeStamp(ChangeStamp changeStamp) {
		this.changeStamp = changeStamp;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}