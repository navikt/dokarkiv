package no.nav.dokarkiv.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Domain entity that represents saksrelasjon.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Entity
@Table(name = "T_SAKSRELASJON")
@Builder
@AllArgsConstructor
public class Saksrelasjon extends AbstractPersistentVersionedDomainObjectWithKilde {

	/** ID used for serialization. */
	private static final long serialVersionUID = ***gammelt_fnr***0038569L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "saksrelasjon_seq")
	@GenericGenerator(name = "saksrelasjon_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
					  parameters = { @Parameter(name = "sequence_name", value = "T_SAKSRELASJON_SEQ"), 
									 @Parameter(name = "initial_value", value = "200000000") })
	@Column(name = "saksrelasjon_id", nullable = false)
	private Long saksrelasjonId;

	@Column(name = "sak_nr_fk", nullable = false)
	private String sakId;

	@Column(name = "feilregistrert")
	private Boolean feilregistrert;

	@Column(name = "endret_av_navn")
	private String endretAvNavn;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_fagsystem", nullable = false)
	private FagsystemCode fagsystem;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;
	
	/**
	 * Defualt constructor.
	 */
	public Saksrelasjon() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 * 
	 * @param saksrelasjonId
	 *            DB-id for the instance.
	 * @param version
	 *            DB-version for the instance.
	 */
	public Saksrelasjon(Long saksrelasjonId, long version) {
		this.saksrelasjonId = saksrelasjonId;
		setVersion(version);
	}
	
	/** {@inheritDoc} */
	@Override
	public Long getId() {
		return getSaksrelasjonId();
	}

	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyMandatoryFieldsNotEndretAvNavn();
		if (saksrelasjonId != null) {
			verifyStringNotBlank(endretAvNavn, "endretAvNavn");
		}
	}

	public void verifyMandatoryFieldsNotEndretAvNavn() {
		verifyStringNotBlank(sakId, "sakId");
		verifyFieldNotNull(fagsystem, "fagsystem");
	}

	/**
	 * Getter for the endretAvNavn property.
	 * 
	 * @return the endretAvNavn
	 */
	public String getEndretAvNavn() {
		return endretAvNavn;
	}

	/**
	 * Setter for the endretAvNavn property.
	 * 
	 * @param endretAvNavn
	 *            the endretAvNavn to set
	 */
	public void setEndretAvNavn(String endretAvNavn) {
		this.endretAvNavn = endretAvNavn;
	}

	/**
	 * Getter for the feilregistrert property.
	 * 
	 * @return the feilregistrert
	 */
	public Boolean getFeilregistrert() {
		return feilregistrert;
	}

	/**
	 * Setter for the feilregistrert property.
	 * 
	 * @param feilregistrert
	 *            the feilregistrert to set
	 */
	public void setFeilregistrert(Boolean feilregistrert) {
		this.feilregistrert = feilregistrert;
	}

	/**
	 * Getter for the sakNr property.
	 * 
	 * @return the sakNr
	 */
	public String getSakId() {
		return sakId;
	}

	/**
	 * Setter for the sakNr property.
	 * 
	 * @param sakId
	 *            the sakNr to set
	 */
	public void setSakId(String sakId) {
		this.sakId = sakId;
	}

	/**
	 * Getter for the saksrelasjonId property.
	 * 
	 * @return the saksrelasjonId
	 */
	public Long getSaksrelasjonId() {
		return saksrelasjonId;
	}
	
	/**
	 * Getter for the fagsystem property.
	 *
	 * @return the fagsystem
	 */
	public FagsystemCode getFagsystem() {
		return fagsystem;
	}

	/**
	 * Setter for the fagsystem property.
	 *
	 * @param fagsystem the fagsystem to set
	 */
	public void setFagsystem(FagsystemCode fagsystem) {
		this.fagsystem = fagsystem;
	}

	/**
	 * Getter for the journalpost property.
	 *
	 * @return the journalpost
	 */
	public Journalpost getJournalpost() {
		return journalpost;
	}

	/**
	 * Setter for the journalpost property.
	 *
	 * @param journalpost the journalpost to set
	 */
	void setJournalpost(Journalpost journalpost) {
		this.journalpost = journalpost;
	}
	
}
