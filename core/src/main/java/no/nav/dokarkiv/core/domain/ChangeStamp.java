package no.nav.dokarkiv.core.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

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
 *
 * This class holds information about:
 * <ul>
 * <li>Who created the object embedding this <code>ChangeStamp</code> object</li>
 * <li>When was the object embedding this <code>ChangeStamp</code> object created</li>
 * <li>Who made changes to the object embedding this <code>ChangeStamp</code> object</li>
 * <li>When was the changes made to the object embedding this <code>ChangeStamp</code> object</li>
 * </ul>
 *
 *
 * @author Morten Andersen-Gott (Accenture)
 *
 */
@Embeddable
public class ChangeStamp implements Serializable {

	private static final long serialVersionUID = ***gammelt_fnr***562288L;

	@Column(name = "created_by", insertable = true, updatable = false)
	private String createdBy;

	@Column(name = "created_date", insertable = true, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "updated_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDate;

	/**
	 * Constructs a new ChangeStamp. The constructor should only be called once, when the object embedding this
	 * <code>ChangeStamp</code> object is actually created for the first time.
	 *
	 * @param userId
	 *            the user id that creates object embedding this <code>ChangeStamp</code> object
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
	 * @param createdBy
	 *            Created by user id
	 * @param createdDate
	 *            Creation date
	 * @param updatedBy
	 *            Last updated by user id
	 * @param updatedDate
	 *            Last updated date
	 */
	public ChangeStamp(String createdBy, Date createdDate, String updatedBy, Date updatedDate) {
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.updatedBy = updatedBy;
		this.updatedDate = updatedDate;
	}

	/**
	 * Method called whenever the object embedding this <code>ChangeStamp</code> object has been updated.
	 *
	 * @param userId
	 *            user id that made the update
	 */
	public void updatedBy(String userId) {
		updatedBy = userId;
		updatedDate = new Date();
	}

	/**
	 * Gets the change by.
	 *
	 * @return username that made the change
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Gets created at date.
	 *
	 * @return sql timestamp for the creation of the object embedding <code>this</code>
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Gets changed at date.
	 *
	 * @return sql timestamp for the most recent change to the object embedding <code>this</code>
	 */
	public Date getUpdatedDate() {
		return updatedDate;
	}

	/**
	 * Gets the user id that made the most recent change.
	 *
	 * @return user id of user that made the most recent change to the object embedding <code>this</code>
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("createdBy", createdBy);
		builder.append("createdDate", createdDate);
		builder.append("updatedBy", updatedBy);
		builder.append("updatedDate", updatedDate);

		return builder.toString();
	}

}
