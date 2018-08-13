package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Domain object that maps against table T_DOK_URL_INFO.
 * 
 * @author Magnus Skuland, Sirius IT
 * @author Eirik Bergande, Sirius IT
 */
@Entity
@Table(name = "T_DOK_URL_INFO")
@Builder
@AllArgsConstructor
public class DokumentUrlInfo extends AbstractPersistentVersionedDomainObject {

	/** ID used for serialization. */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dokumentUrlInfo_seq")
	@GenericGenerator(name = "dokumentUrlInfo_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
					  parameters = { @Parameter(name = "sequence_name", value = "T_DOK_URL_INFO_SEQ"), 
									 @Parameter(name = "initial_value", value = "200000000") })
	@Column(name = "dok_url_info_id", nullable = false)
	private Long dokumentUrlInfoId;

	@Column(name = "doctoken", nullable = false, length = 36)
	private String doctoken;

	@ManyToOne
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	@Column(name = "tidspunkt", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date tidspunkt;

	@Column(name = "fil_uuid", nullable = false, length = 36)
	private String filUuid;
	
	@Column(name = "ttl_minutes")
	private Long timeToLiveMinutes;
	
	/**
	 * Default constructor.
	 */
	public DokumentUrlInfo() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 * 
	 * @param dokumentUrlInfoId
	 *            DB-id for the instance.
	 * @param version
	 *            DB-version for the instance.
	 */
	public DokumentUrlInfo(Long dokumentUrlInfoId, long version) {
		this.dokumentUrlInfoId = dokumentUrlInfoId;
		setVersion(version);
	}
	
	/**
	 * Getter for the docToken property.
	 * 
	 * @return the docToken
	 */
	public String getDoctoken() {
		return doctoken;
	}

	/**
	 * Setter for the docToken property.
	 * 
	 * @param doctoken
	 *            the docToken to set
	 */
	public void setDoctoken(String doctoken) {
		this.doctoken = doctoken;
	}

	/**
	 * Getter for the dokumentUrlInfoId property.
	 * 
	 * @return the dokumentUrlInfoId
	 */
	public Long getDokumentUrlInfoId() {
		return dokumentUrlInfoId;
	}

	/**
	 * Setter for the dokumentUrlInfoId property.
	 * 
	 * @param dokumentUrlInfoId
	 *            the dokumentUrlInfoId to set
	 */
	public void setDokumentUrlInfoId(Long dokumentUrlInfoId) {
		this.dokumentUrlInfoId = dokumentUrlInfoId;
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
	 * @param journalpost
	 *            the journalpost to set
	 */
	public void setJournalpost(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

	/**
	 * Getter for the tidspunkt property.
	 * 
	 * @return the tidspunkt
	 */
	public Date getTidspunkt() {
		if (tidspunkt != null) {
			return new Date(tidspunkt.getTime());
		}
		return null;
	}

	/**
	 * Setter for the tidspunkt property.
	 * 
	 * @param tidspunkt
	 *            the tidspunkt to set
	 */
	public void setTidspunkt(Date tidspunkt) {
		if (tidspunkt != null) {
			this.tidspunkt = new Date(tidspunkt.getTime());
		} else {
			this.tidspunkt = null;
		}
	}

	/**
	 * Getter for the filUuid property.
	 *
	 * @return the filUuid
	 */
	public String getFilUuid() {
		return filUuid;
	}

	/**
	 * Setter for the filUuid property.
	 *
	 * @param filUuid the filUuid to set
	 */
	public void setFilUuid(String filUuid) {
		this.filUuid = filUuid;
	}

	/**
	 * Getter for the timeToLiveMinutes property.
	 *
	 * @return the timeToLiveMinutes
	 */
	public Long getTimeToLiveMinutes() {
		return timeToLiveMinutes;
	}

	/**
	 * Setter for the timeToLiveMinutes property.
	 *
	 * @param timeToLiveMinutes the timeToLiveMinutes to set
	 */
	public void setTimeToLiveMinutes(Long timeToLiveMinutes) {
		this.timeToLiveMinutes = timeToLiveMinutes;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("journalpostId", journalpost.getJournalpostId())
			.append("tidspunkt", tidspunkt)
			.append("docToken", doctoken)
			.append("filUuid", filUuid)
			.append("timeToLiveMinutes", timeToLiveMinutes)
			.toString();
	}
}
