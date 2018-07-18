package no.nav.dokarkiv.core.stelvio;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Abstract base class for classes representing a codes table's entries, that is, rows in the codes table's corresponding
 * database tables where code is used as the key in the table. <p/> In addition to the capabilities in
 * <code>CodesTableItem</code>, this also specifies a time period in which the instances are valid. <p/> This class is a
 * <code>MappedSuperclass</code>, meaning that Entities that inherits from this class must map to a table that defines
 * columns set up by this class
 *
 * @author Therese Steensen (Accenture)
 * @author Stig Kleppe-Jørgensen (Accenture)
 * @version $Id$
 * @deprecated for mye komplisert logikk som ikke brukes. burde konsolideres
 *
 * @param <K>
 *            an enum type variable
 * @param <V>
 *            a type variable
 */
@Deprecated
@MappedSuperclass
public abstract class CodesTablePeriodicItem<K extends Enum, V> extends AbstractCodesTablePeriodicItem<K, V> {
	private static final long serialVersionUID = -***gammelt_fnr***40432765L;

	/** The code for this item. */
	@Id
	@Column(name = "code")
	private String code;

	/**
	 * Constructs a new instance. Should only be used by the persistence provider and in some cases architecture code for
	 * mapping between layers.
	 */
	protected CodesTablePeriodicItem() {
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCodeAsString() {
		return code;
	}

	/**
	 * Must NOT be used except when using the CodesTableManager is not possible. CodesTableItems should be instantiated through
	 * the CodesTableManager.
	 *
	 * @param code
	 *            The code value to set
	 */
	public void setCodeAsString(String code) {
		this.code = code;
	}
}
