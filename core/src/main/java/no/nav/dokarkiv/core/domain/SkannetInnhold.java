package no.nav.dokarkiv.core.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Domain entity that represents skannet innhold.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Per Kristian Foss, Visma Sirius
 */
@Entity
@Table(name = "T_SKANNET_INNHOLD")
public class SkannetInnhold extends AbstractPersistentVersionedDomainObjectWithKilde {

	/** ID used for serialization. */
	private static final long serialVersionUID = ***gammelt_fnr***94040373L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skannetInnhold_seq")
	@GenericGenerator(name = "skannetInnhold_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
					  parameters = { @Parameter(name = "sequence_name", value = "T_SKANNET_INNHOLD_SEQ"), 
									 @Parameter(name = "initial_value", value = "200000000") })
	@Column(name = "skannet_innhold_id", nullable = false)
	private Long skannetInnholdId;

	@Column(name = "vedlegg_nr")
	private Integer vedleggNr;

	@Column(name = "vedlegg_innhold")
	private String vedleggInnhold;
	
	@Column(name = "dokumenttypeId")
	private String dokumenttypeId;

	/**
	 * Default constructor.
	 */
	public SkannetInnhold() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 * 
	 * @param skannetInnholdId
	 *            DB-id for the instance.
	 * @param version
	 *            DB-version for the instance.
	 */
	public SkannetInnhold(Long skannetInnholdId, long version) {
		this.skannetInnholdId = skannetInnholdId;
		setVersion(version);
	}

	/** {@inheritDoc} */
	@Override
	public Long getId() {
		return getSkannetInnholdId();
	}
	
	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyStringNotBlank(vedleggInnhold, "vedleggInnhold");
	}
	
	/**
	 * Getter for the skannetInnholdId property.
	 * 
	 * @return the skannetInnholdId
	 */
	public Long getSkannetInnholdId() {
		return skannetInnholdId;
	}

	/**
	 * Getter for the vedleggInnhold property.
	 * 
	 * @return the vedleggInnhold
	 */
	public String getVedleggInnhold() {
		return vedleggInnhold;
	}

	/**
	 * Setter for the vedleggInnhold property.
	 * 
	 * @param vedleggInnhold
	 *            the vedleggInnhold to set
	 */
	public void setVedleggInnhold(String vedleggInnhold) {
		this.vedleggInnhold = vedleggInnhold;
	}

	/**
	 * Getter for the vedleggNr property.
	 * 
	 * @return the vedleggNr
	 */
	public Integer getVedleggNr() {
		return vedleggNr;
	}

	/**
	 * Setter for the vedleggNr property.
	 * 
	 * @param vedleggNr
	 *            the vedleggNr to set
	 */
	public void setVedleggNr(Integer vedleggNr) {
		this.vedleggNr = vedleggNr;
	}
	
	/**
	 * Getter for the dokumenttypeId property
	 *
	 * @return the dokumenttypeId
	 */
	public String getDokumenttypeId() {
		return dokumenttypeId;
	}
	
	/**
	 * Setter for the dokumenttypeId property
	 *
	 * @param dokumenttypeId
	 * 				the dokumenttypeId to set
	 */
	public void setDokumenttypeId(String dokumenttypeId) {
		this.dokumenttypeId = dokumenttypeId;
	}
}
