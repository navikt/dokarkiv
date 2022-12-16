package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
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
 * Domain object that represents kryssreferanse
 * 
 * @author Per Kristian Foss, Visma Sirius
 */
@Builder
@AllArgsConstructor
@Entity
@Table(name = "T_KRYSSREFERANSE")
public class Kryssreferanse extends AbstractPersistentVersionedDomainObjectWithKilde {

	/** ID for serialization */
	private static final long serialVersionUID = 2970255498067421424L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kryssreferanse_seq")
	@GenericGenerator(name = "kryssreferanse_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
					  parameters = { @Parameter(name = "sequence_name", value = "T_KRYSSREFERANSE_SEQ") })
	@Column(name = "kryssreferanse_id", nullable = false)
	private Long kryssreferanseId;
	
	@Column(name = "referanse_id", nullable = false, length = 20)
	private String referanseId;
	
	@Column(name = "referanse_nr")
	private Long referanseNr;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "k_referanse_t", nullable = false, length = 20)
	private ReferanseTypeCode referanseType;
	
	/**
	 * Default constructor.
	 */
	public Kryssreferanse() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 * 
	 * @param kryssreferanseId
	 *            DB-id for the instance.
	 * @param version
	 *            DB-version for the instance.
	 */
	public Kryssreferanse(Long kryssreferanseId, long version) {
		this.kryssreferanseId = kryssreferanseId;
		setVersion(version);
	}

	/** {@inheritDoc} */
	@Override
	public Long getId() {
		return getKryssreferanseId();
	}
	
	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyStringNotBlank(referanseId, "referanseId");
		verifyFieldNotNull(referanseType, "referanseType");
	}
	
	/**
	 * Getter for the referanseId property.
	 *
	 * @return the referanseId
	 */
	public String getReferanseId() {
		return referanseId;
	}

	/**
	 * Setter for the referanseId property.
	 *
	 * @param referanseId the referanseId to set
	 */
	public void setReferanseId(String referanseId) {
		this.referanseId = referanseId;
	}

	/**
	 * Getter for the referanseNr property.
	 *
	 * @return the referanseNr
	 */
	public Long getReferanseNr() {
		return referanseNr;
	}

	/**
	 * Setter for the referanseNr property.
	 *
	 * @param referanseNr the referanseNr to set
	 */
	public void setReferanseNr(Long referanseNr) {
		this.referanseNr = referanseNr;
	}

	/**
	 * Getter for the referanseType property.
	 *
	 * @return the referanseType
	 */
	public ReferanseTypeCode getReferanseType() {
		return referanseType;
	}

	/**
	 * Setter for the referanseType property.
	 *
	 * @param referanseType the referanseType to set
	 */
	public void setReferanseType(ReferanseTypeCode referanseType) {
		this.referanseType = referanseType;
	}

	/**
	 * Getter for the kryssreferanseId property.
	 *
	 * @return the kryssreferanseId
	 */
	public Long getKryssreferanseId() {
		return kryssreferanseId;
	}

}
