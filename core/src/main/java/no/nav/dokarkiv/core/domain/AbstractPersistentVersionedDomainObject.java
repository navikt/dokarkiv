package no.nav.dokarkiv.core.domain;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * Abstract base class for all persistent domain objects with version field.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractPersistentVersionedDomainObject extends AbstractPersistentDomainObject {

	@Version
	@Column(name = "versjon", nullable = false)
	private long version;

	/**
	 * Getter for the version property.
	 *
	 * @return the version
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Setter for the version property.
	 *
	 * @param version the version to set
	 */
	protected void setVersion(long version) {
		this.version = version;
	}
	
	/**
	 * Checks that a field is not null.
	 * 
	 * @param fieldValue The value to check.
	 * @param fieldName THe fieldName.
	 */
	protected void verifyFieldNotNull(Object fieldValue, String fieldName) {
		if (fieldValue == null) {
			throwExceptionForMissingField(fieldName);
		}
	}
	
	/**
	 * Checks that a String is not null or empty.
	 * 
	 * @param fieldValue The String to check.
	 * @param fieldName The fieldName.
	 */
	protected void verifyStringNotBlank(String fieldValue, String fieldName) {
		if (isBlank(fieldValue)) {
			throwExceptionForMissingField(fieldName);
		}
	}
	
	/**
	 * Throws exception when a mandatory field is missing.
	 * 
	 * @param fieldName The missing field.
	 */
	protected void throwExceptionForMissingField(String fieldName) {
		throw new InvalidArgumentException(this.getClass().getSimpleName() + "." + fieldName + " must be set");
	}
	
}