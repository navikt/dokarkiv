package no.nav.dokarkiv.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

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
	private LocalDateTime createdDate;

	@Column(name = "endret_av", length = 40)
	private String updatedBy;

	@Column(name = "dato_endret")
	private LocalDateTime updatedDate;

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
	public ChangeStamp(String createdBy, LocalDateTime createdDate, String updatedBy, LocalDateTime updatedDate) {
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.updatedBy = updatedBy;
		this.updatedDate = updatedDate;
	}

	/**
	 * Method called whenever the object embedding this <code>ChangeStamp</code> object has been updated.
	 *
	 * @param userId user id that made the update
	 */
	void updatedBy(String userId) {
		updatedBy = userId;
		updatedDate = LocalDateTime.now();
	}
}
