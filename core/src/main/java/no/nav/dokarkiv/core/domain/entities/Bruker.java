package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.validator.BrukerValidator;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Domain entity class that represents bruker.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Per Kristian Foss, Visma Sirius
 */
@Entity
@Table(name = "T_BRUKER")
@Builder(toBuilder = true)
@AllArgsConstructor
public class Bruker extends AbstractPersistentVersionedDomainObjectWithKilde {

	/** ID used for serialization. */
	private static final long serialVersionUID = -***gammelt_fnr***99426224L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brukerInfo_seq")
	@GenericGenerator(name = "brukerInfo_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
					  parameters = { @Parameter(name = "sequence_name", value = "T_BRUKER_SEQ"),
									 @Parameter(name = "initial_value", value = "200000000") })
	@Column(name = "brukerinfo_id", nullable = false)
	private Long brukerInfoId;

	@Column(name = "bruker_id", length = 11, nullable = false)
	private String brukerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_bruker_t", nullable = false)
	private BrukerTypeCode brukerType;

	/**
	 * Default constructor.
	 */
	public Bruker() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param brukerInfoId
	 *            DB-id for the instance.
	 * @param version
	 *            DB-version for the instance.
	 */
	public Bruker(Long brukerInfoId, long version) {
		this.brukerInfoId = brukerInfoId;
		setVersion(version);
	}

	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyStringNotBlank(brukerId, "brukerId");
		verifyFieldNotNull(brukerType, "brukerType");
		validateBrukerId();
	}

	private void validateBrukerId() {
		BrukerValidator.validate(this);
	}

	/** {@inheritDoc} */
	@Override
	public Long getId() {
		return getBrukerInfoId();
	}

	/**
	 * Getter for the brukerId property.
	 *
	 * @return the brukerId
	 */
	public String getBrukerId() {
		return brukerId;
	}

	/**
	 * Setter for the brukerId property.
	 *
	 * @param brukerId
	 *            the brukerId to set
	 */
	public void setBrukerId(String brukerId) {
		this.brukerId = brukerId;
	}

	/**
	 * Getter for the brukerInfoId property.
	 *
	 * @return the brukerInfoId
	 */
	public Long getBrukerInfoId() {
		return brukerInfoId;
	}

	/**
	 * Getter for the gjelderType property.
	 *
	 * @return the brukertype
	 */
	public BrukerTypeCode getBrukerType() {
		return brukerType;
	}

	/**
	 * Setter for the brukerType property.
	 *
	 * @param brukerType the brukerType to set
	 */
	public void setBrukerType(BrukerTypeCode brukerType) {
		this.brukerType = brukerType;
	}
}
