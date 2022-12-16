package no.nav.dokarkiv.core.domain;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Class used to represent audit information for an entity in the database. This object is embeddedable, meaning it can be used
 * inside a JPA Entity.
 * <p>
 * This object is used by embedding it in the object that contains the maps to a table that contains audit columns. Objects that
 * embed this object should have a parameterized constructor that takes a new <code>ChangeStamp</code>. After an
 * <code>ChangeStamp</code> has been created for an object it should never be replaced by a new one and only be modified by the
 * {@link #updatedBy(String)} method.
 * </p>
 * <p>
 * <em>
 * It's the responsibility of the application developer to ensure that {@link #updatedBy(String)}
 * is being called when changes are made.
 * </em>
 * </p>
 * <p>
 * This class holds information about:
 * <ul>
 * <li>Who created the object embedding this <code>ChangeStamp</code> object</li>
 * <li>When was the object embedding this <code>ChangeStamp</code> object created</li>
 * <li>Who made changes to the object embedding this <code>ChangeStamp</code> object</li>
 * <li>When was the changes made to the object embedding this <code>ChangeStamp</code> object</li>
 * </ul>
 */
@Embeddable
@Getter
@ToString
public class ChangeStamp implements Serializable {

	private static final long serialVersionUID = 61541164562562288L;

	@Column(name = "opprettet_av", updatable = false, nullable = false, length = 40)
	private String createdBy;

	@Column(name = "dato_opprettet", updatable = false, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name = "endret_av", length = 40)
	private String updatedBy;

	@Column(name = "dato_endret")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDate;

	/**
	 * Constructs a new ChangeStamp. The constructor should only be called once, when the object embedding this
	 * <code>ChangeStamp</code> object is actually created for the first time.
	 *
	 * @param userId the user id that creates object embedding this <code>ChangeStamp</code> object
	 */
	public ChangeStamp(String userId) {
		this.createdBy = userId;
		this.updatedBy = userId;

		// Updated and Created times are equal at first
		Date now = new Date();
		this.createdDate = now;
		this.updatedDate = now;
	}

	/**
	 * No-arg constructor should only be used by persistence provider. The application should use the parameterized constructor.
	 */
	protected ChangeStamp() {
	}

	/**
	 * Constructor with arguments. This constructor should only be used by mappers, to populate an already existing
	 * changestamp!. For creating new changestamps, use the constructor with user id as parameter.
	 *
	 * @param createdBy   Created by user id
	 * @param createdDate Creation date
	 * @param updatedBy   Last updated by user id
	 * @param updatedDate Last updated date
	 */
	public ChangeStamp(String createdBy, Date createdDate, String updatedBy, Date updatedDate) {
		this.createdBy = createdBy;
		this.createdDate = createdDate == null ? null : new Date(createdDate.getTime());
		this.updatedBy = updatedBy;
		this.updatedDate = updatedDate == null ? null : new Date(updatedDate.getTime());
	}

	/**
	 * Method called whenever the object embedding this <code>ChangeStamp</code> object has been updated.
	 *
	 * @param userId user id that made the update
	 */
	public void updatedBy(String userId) {
		updatedBy = userId;
		updatedDate = new Date();
	}
}
