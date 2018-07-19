package no.nav.dokarkiv.core.stelvio;

import static no.nav.dokarkiv.core.stelvio.Internal.cast;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Abstract base class for classes representing a codes table's entries, that is, rows in the codes table's corresponding
 * database tables.
 * <p/>
 * In addition to the capabilities in <code>AbstractCodesTableItem</code>, this also specifies a time period in which the
 * instances are valid.
 * <p/>
 * For internal usage only, containing common code.
 * <p/>
 * This class is a <code>MappedSuperclass</code>, meaning that Entities that inherits from this class must map to a table that
 * defines columns set up by this class.
 *
 * @author Stig Kleppe-Jorgense, Accenture
 * @version $Id$
 * @see AbstractCodesTableItem
 * @deprecated for mye komplisert logikk som ikke brukes. burde konsolideres
 *
 * @param <K>
 *            an enum type variable
 * @param <V>
 *            a type variable
 */
@Deprecated
@MappedSuperclass
public abstract class AbstractCodesTablePeriodicItem<K extends Enum, V> extends AbstractCodesTableItem<K, V> {
	/** The date the item is valid from. */
	@Column(name = "FROM_DATE")
	@Temporal(TemporalType.DATE)
	private Date fromDate;

	/** The date the item is valid to. */
	@Column(name = "TO_DATE")
	@Temporal(TemporalType.DATE)
	private Date toDate;

	/**
	 * Constructs a new instance. Should only be used by the persistence provider and in some cases architecture code for
	 * mapping between layers.
	 */
	protected AbstractCodesTablePeriodicItem() {
	}

	/**
	 * Returns the date the item is valid from.
	 *
	 * @return The date the item is valid from.
	 */
	public Date getFromDate() {
		return fromDate == null ? null : new Date(fromDate.getTime());
	}

	/**
	 * Returns the date the item is valid to.
	 *
	 * @return The date the item is valid to.
	 */
	public Date getToDate() {
		return toDate == null ? null : new Date(toDate.getTime());
	}

	/**
	 * Compares this instance with another object.
	 * <p/>
	 * The method returns true if the other object is not null, is of the same class as this and
	 * <code>other.getCodeAsString()</code> equals <code>this.getCodeAsString()</code> and the time periods are the same.
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
	 *            the object to compare to
	 * @return true if equal, false otherwise.
	 */
	public boolean equals(Object other) {

		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (other.getClass() != getClass()) {
			return false;
		}

		AbstractCodesTablePeriodicItem<K, V> castOther = cast(other);
		return new EqualsBuilder().append(getCodeAsString(), castOther.getCodeAsString()).append(getFromDate(),
				castOther.getFromDate()).append(getToDate(), castOther.getToDate()).isEquals();
	}

	/**
	 * Returns a <code>String</code> representation of this object.
	 *
	 * @return the string representation of the object
	 */
	public String toString() {
		return new ToStringBuilder(this).append("code", getCodeAsString()).append("decode", getDecode()).append("fromDate",
				getFromDate()).append("toDate", getToDate()).append("isValid", isValid()).toString();
	}

	/**
	 * Compares code, fromDate and toDate
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
	 * @param o
	 *            the AbstractCodesTablePeriodicItem to be compared
	 * @return an integer representing the comparison result
	 */
	@Override
	public int compareTo(Object o) {
		// This may throw classcastexception, ok according to Comparable-contract
		AbstractCodesTablePeriodicItem other = (AbstractCodesTablePeriodicItem) o;

		// Normally it should never be the case that the from date is empty
		int compareFromDate = 0;
		if (getFromDate() == null && other.getFromDate() == null) {
			compareFromDate = 0;
		} else if (getFromDate() == null) {
			compareFromDate = 1;
		} else if (other.getFromDate() == null) {
			compareFromDate = -1;
		} else {
			this.getFromDate().compareTo(other.getFromDate());
		}

		int compareToDate;
		// Null date is considered "bigger" than any other date
		if (getToDate() == null && other.getToDate() == null) {
			compareToDate = 0;
		} else if (getToDate() == null) {
			compareToDate = 1;
		} else if (other.getToDate() == null) {
			compareToDate = -1;
		} else {
			compareToDate = getToDate().compareTo(other.getToDate());
		}

		if (super.compareTo(o) != 0) {
			return super.compareTo(o);
		} else if (compareFromDate != 0) {
			return compareFromDate;
		} else {
			return compareToDate;
		}
	}

	/**
	 * {@inheritDoc} Uses super.hashCode in addition to fromDate and toDate to calculate hashCode.
	 *
	 * @return hashCode
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().appendSuper(super.hashCode()).append(fromDate).append(toDate).toHashCode();
	}
}