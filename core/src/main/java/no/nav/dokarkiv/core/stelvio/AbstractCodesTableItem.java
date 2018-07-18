package no.nav.dokarkiv.core.stelvio;

import static no.nav.dokarkiv.core.stelvio.Internal.cast;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

/**
 * Abstract base class for classes representing a codes table's entries, that is, rows in the codes table's corresponding
 * database tables. <p/> For internal usage only, containing common code. <p/> This class is a <code>MappedSuperclass</code>,
 * meaning that Entities that inherits from this class must map to a table that defines columns set up by this class
 *
 * @param <K>
 *            an enum
 * @param <V>
 *            an object
 *
 * @author Therese Steensen (Accenture)
 * @author Morten Andersen-Gott (Accenture)
 * @author Stig Kleppe-Jørgensen (Accenture)
 * @version $Id$
 * @deprecated for mye komplisert logikk som ikke brukes. burde konsolideres
 */
@Deprecated
@MappedSuperclass
public abstract class AbstractCodesTableItem<K extends Enum, V> implements Serializable, Comparable {
	private static final long serialVersionUID = -***gammelt_fnr***1433404L;

	/** The decode of an item, often it's message. */
	@Column(name = "decode")
	private V decode;

	/** Holds the looked up enum value corresponding to the String value in code. Should not be persisted. */
	@Transient
	private K codeAsEnum;

	/** If the item is valid or not. */
	@Column(name = "valid")
	private boolean valid;

	/**
	 * Constructs a new instance. Should only be used by the persistence provider and in some cases architecture code for
	 * mapping between layers.
	 */
	protected AbstractCodesTableItem() {
	}

	/**
	 * Returns the code represented by this item.
	 *
	 * @return The item's code.
	 */
	@SuppressWarnings("unchecked")
	public K getCode() {
		// Cache the lookup
		if (codeAsEnum == null) {
			Class<K> genericType = getCodeClass(getClass());
			// Check if the enum used in CodesTableItem instance implements IllegalCodeEnum
			if (IllegalCodeEnum.class.isAssignableFrom(genericType)) {
				K[] constants = genericType.getEnumConstants(); // Get all Enum constants for this enum
				for (K k : constants) { // Loop through enum constants
					String stringCode = ((IllegalCodeEnum) k).getIllegalCode(); // Get illegalEnumCode for constant
					if (stringCode.equals(getCodeAsString())) { // Check if it matches this items' codeAsString
						codeAsEnum = k; // Correct enum found, cache
						break;
					}
				}
			} else { // Regular enum where enum.name() is equal to codeAsString
				codeAsEnum = (K) Enum.valueOf(genericType, getCodeAsString());
			}
		}

		return codeAsEnum;
	}

	/**
	 * Returns the code represented by this item as a <code>String</code>.
	 *
	 * @return The item's code as a <code>String</code>.
	 */
	public abstract String getCodeAsString();

	/**
	 * Returns the class of the code, that is, the sub type of Enum defined for the specified class.
	 *
	 * @param <T>
	 *            the type of elements maintained by this set
	 * @param clazz
	 *            the sub class of <code>AbstractCodesTableItem</code>.
	 * @return the class of the code.
	 */
	static <T extends Enum> Class<T> getCodeClass(Class clazz) {
		return cast(((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	/**
	 * Returns the decode represented by this item.
	 *
	 * @return The item's decode.
	 */
	public V getDecode() {
		return decode;
	}

	/**
	 * Returns the validity of this item.
	 *
	 * @return <code>true</code> if item is valid, <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Checks that the given enum is equal to this instance's code by comparing their values as <code>String</code>s. Uses
	 * the <code>String</code> value to prevent problems with looking up an enum constant that does not exist.
	 *
	 * @param otherCode
	 *            the enum constant to check for equality with.
	 * @return true if the value of our instance's code and the specified enum's value.
	 */
	public boolean isCodeEqualTo(K otherCode) {
		return getCodeAsString().equals(getStringCodeFromEnumCode(otherCode));
	}

	/**
	 * Compares this instance with another object. <p/> The method returns true if the other object is not null, is of the same
	 * class as this and <code>other.getCodeAsString()</code> equals <code>this.getCodeAsString()</code>
	 *
	 * <strong>
	 * <p>
	 * It's vital that
	 *
	 * <pre>
	 * itemA.equals(itemB) == (itemA.compareTo(itemB) == 0)
	 * </pre>
	 *
	 * The statements must return the same value, as AbstractCodesTableItem implementations will be added to
	 * SortedSet/SortedMap. Sorted set (or sorted map) violates the general contract for set (or map), which is defined in terms
	 * of the equals method.
	 * </p>
	 * </strong>
	 *
	 * @param other
	 *            the object
	 * @return true if equal, false otherwise.
	 * @see Comparable
	 */
	public boolean equals(Object other) {
		if (null == other) {
			return false;
		}

		if (!getClass().equals(other.getClass())) {
			return false;
		}

		AbstractCodesTableItem<K, V> castOther = cast(other);
		if ((new EqualsBuilder().append(getCodeAsString(), castOther.getCodeAsString()).isEquals())) {
			return (true);
		}

		return (false);
	}

	/**
	 * Hash code is computed based on this instance's code.
	 *
	 * @return the computed hash code.
	 */
	public int hashCode() {
		return new HashCodeBuilder().append(getCodeAsString()).toHashCode();
	}

	/**
	 * Returns a <Code>String</code> representation of this object.
	 *
	 * @return a <Code>String</code> representation of this object.
	 */
	public String toString() {
		return new ToStringBuilder(this).append("code", getCodeAsString()).append("decode", getDecode()).append("isValid",
				isValid()).toString();
	}

	/**
	 * {@inheritDoc} Compares the this and other code's
	 *
	 * <strong>
	 * <p>
	 * It's vital that
	 *
	 * <pre>
	 * itemA.equals(itemB) == (itemA.compareTo(itemB) == 0)
	 * </pre>
	 *
	 * The statements must return the same value, as AbstractCodesTableItem implementations will be added to
	 * SortedSet/SortedMap. Sorted set (or sorted map) violates the general contract for set (or map), which is defined in terms
	 * of the equals method.
	 * </p>
	 * </strong>
	 *
	 *
	 * @see Comparable
	 */
	public int compareTo(Object o) {

		AbstractCodesTableItem cti = (AbstractCodesTableItem) o;

		if (cti == null) {
			return (-1);
		}

		int codeCompare = getCodeAsString().compareTo(cti.getCodeAsString());
		return codeCompare;

	}

	/**
	 * Will return code.getIllegalCode if code is instance of {@link IllegalCodeEnum} otherwise it will return code.name().
	 *
	 * @param enumCode
	 *            identifying CodesTableItem in persistence store
	 * @return the illegal code or the enum code name.
	 */
	protected String getStringCodeFromEnumCode(K enumCode) {
		return (enumCode instanceof IllegalCodeEnum) ? ((IllegalCodeEnum) enumCode).getIllegalCode() : enumCode.name();
	}
}
