package no.nav.dokarkiv.core.domain;

import no.nav.dokarkiv.core.domain.codes.ArsakReturCode;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Domain object that represents returinfo
 * 
 * @author Per Kristian Foss, Visma Sirius
 */
@Entity
@Table(name = "T_RETUR_INFO")
public class ReturInfo extends AbstractPersistentVersionedDomainObjectWithKilde {

	/** ID for serialization */
	private static final long serialVersionUID = ***gammelt_fnr***46511632L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "returInfo_seq")
	@GenericGenerator(name = "returInfo_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
					  parameters = { @Parameter(name = "sequence_name", value = "T_RETUR_INFO_SEQ") })
	@Column(name = "retur_info_id", nullable = false)
	private Long returInfoId;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_retur", nullable = false)
	private Date returDato;
	
	@Column(name = "adr_sendt_igjen")
	private String adresseSendtIgjen;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_sendt_igjen")
	private Date sendtIgjenDato;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "k_arsak_retur", nullable = false)
	private ArsakReturCode arsakRetur;
	
	/**
	 * Default constructor.
	 */
	public ReturInfo() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 * 
	 * @param returInfoId
	 *            DB-id for the instance.
	 * @param version
	 *            DB-version for the instance.
	 */
	public ReturInfo(Long returInfoId, long version) {
		this.returInfoId = returInfoId;
		setVersion(version);
	}
	
	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyFieldNotNull(returDato, "returDato");
		verifyFieldNotNull(arsakRetur, "arsakRetur");
	}

	/** {@inheritDoc} */
	@Override
	public Long getId() {
		return getReturInfoId();
	}
	
	/**
	 * Getter for the returInfoId property.
	 *
	 * @return the returInfoId
	 */
	public Long getReturInfoId() {
		return returInfoId;
	}

	/**
	 * Getter for the returDato property.
	 *
	 * @return the returDato
	 */
	public Date getReturDato() {
		if (returDato != null) {
			return new Date(returDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the returDato property.
	 *
	 * @param returDato the returDato to set
	 */
	public void setReturDato(Date returDato) {
		if (returDato != null) {
			this.returDato = new Date(returDato.getTime());
		} else {
			this.returDato = null;
		}
	}

	/**
	 * Getter for the adresseSendtIgjen property.
	 *
	 * @return the adresseSendtIgjen
	 */
	public String getAdresseSendtIgjen() {
		return adresseSendtIgjen;
	}

	/**
	 * Setter for the adresseSendtIgjen property.
	 *
	 * @param adresseSendtIgjen the adresseSendtIgjen to set
	 */
	public void setAdresseSendtIgjen(String adresseSendtIgjen) {
		this.adresseSendtIgjen = adresseSendtIgjen;
	}

	/**
	 * Getter for the sendtIgjenDato property.
	 *
	 * @return the sendtIgjenDato
	 */
	public Date getSendtIgjenDato() {
		if (sendtIgjenDato != null) {
			return new Date(sendtIgjenDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the sendtIgjenDato property.
	 *
	 * @param sendtIgjenDato the sendtIgjenDato to set
	 */
	public void setSendtIgjenDato(Date sendtIgjenDato) {
		if (sendtIgjenDato != null) {
			this.sendtIgjenDato = new Date(sendtIgjenDato.getTime());
		} else {
			this.sendtIgjenDato = null;
		}
	}

	/**
	 * Getter for the arsakRetur property.
	 *
	 * @return the arsakRetur
	 */
	public ArsakReturCode getArsakRetur() {
		return arsakRetur;
	}

	/**
	 * Setter for the arsakRetur property.
	 *
	 * @param arsakRetur the arsakRetur to set
	 */
	public void setArsakRetur(ArsakReturCode arsakRetur) {
		this.arsakRetur = arsakRetur;
	}

}
