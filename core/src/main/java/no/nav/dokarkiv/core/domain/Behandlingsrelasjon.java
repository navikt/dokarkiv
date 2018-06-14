package no.nav.dokarkiv.core.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Domain entity that represents behandlingsrelasjon.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Entity
@Table(name = "T_BEHANDLINGSRELASJON")
public class Behandlingsrelasjon extends AbstractPersistentVersionedDomainObjectWithKilde {
	
	/** Serialization UID */
	private static final long serialVersionUID = ***gammelt_fnr***48432L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "behandlingsrelasjon_seq")
	@GenericGenerator(name = "behandlingsrelasjon_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
					  parameters = { @Parameter(name = "sequence_name", value = "T_BEHANDLINGSRELASJON_SEQ") })
	@Column(name = "behandlingsrelasjon_id", nullable = false)
	private Long behandlingsrelasjonId;
	
	@Column(name = "behandling_id", nullable = false)
	private String behandlingsId;
	
	//TODO Enum
	@Column(name = "behandling_type", nullable = false)
	private String behandlingsType;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	/**
	 * Constructs a new Behandlingsrelasjon.
	 */
	public Behandlingsrelasjon() {
	}

	/**
	 * Constructs a new Behandlingsrelasjon.
	 *
	 * @param behandlingsrelasjonId The id.
	 */
	public Behandlingsrelasjon(Long behandlingsrelasjonId) {
		this.behandlingsrelasjonId = behandlingsrelasjonId;
	}

	/** {@inheritDoc} */
	@Override
	public Long getId() {
		return getBehandlingsrelasjonId();
	}

	/**
	 * Getter for the behandlingsId property.
	 *
	 * @return the behandlingsId
	 */
	public String getBehandlingsId() {
		return behandlingsId;
	}

	/**
	 * Setter for the behandlingsId property.
	 *
	 * @param behandlingsId the behandlingsId to set
	 */
	public void setBehandlingsId(String behandlingsId) {
		this.behandlingsId = behandlingsId;
	}

	/**
	 * Getter for the behandlingsType property.
	 *
	 * @return the behandlingsType
	 */
	public String getBehandlingsType() {
		return behandlingsType;
	}

	/**
	 * Setter for the behandlingsType property.
	 *
	 * @param behandlingsType the behandlingsType to set
	 */
	public void setBehandlingsType(String behandlingsType) {
		this.behandlingsType = behandlingsType;
	}

	/**
	 * Getter for the behandlingsrelasjonId property.
	 *
	 * @return the behandlingsrelasjonId
	 */
	public Long getBehandlingsrelasjonId() {
		return behandlingsrelasjonId;
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
	public void setJournalpost(Journalpost journalpost) {
		this.journalpost = journalpost;
	}
	
}
