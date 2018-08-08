package no.nav.dokarkiv.core.domain.entities.bidrag;

import com.google.common.base.Strings;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain object representing metadata for temporary stored Bidrag documents.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Entity
@Table(name = "T_BIDRAG_MELLOMLAGRING")
@NamedQueries({
	@NamedQuery(name = "BidragMellomlagring.findBidragMellomlagringById",
			query = "select b from BidragMellomlagring b where b.bidragMellomlagringId = :bidragMellomlagringId") })
public class BidragMellomlagring extends AbstractPersistentVersionedDomainObject {

	/** Serialization UID */
	private static final long serialVersionUID = -***gammelt_fnr***56861638L;
	
	public static final Integer ID_PREFIX = 4249;
	private static final int ID_PREFIX_LENGTH = ID_PREFIX.toString().length();
	private static final int ID_WITH_PREFIX_LENGTH = 12;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bidragMellomlagring_seq")
	@GenericGenerator(name = "bidragMellomlagring_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
						parameters = { @Parameter(name = "sequence_name", value = "T_BIDRAG_MELLOMLAGRING_SEQ") })
	@Column(name = "bidrag_mellomlagring_id", nullable = false)
	private Long bidragMellomlagringId;
	
	@Column(name = "avsender_fnr", nullable = false)
	private String avsenderFnr;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_mottatt", nullable = false)
	private Date mottattDato;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private BidragMellomlagringStatus status;
	
	@OneToMany(mappedBy = "bidragMellomlagring")
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH })
	private Set<BidragMellomlagringDokument> bidragMellomlagringDokuments = new HashSet<>();

	/**
	 * Default constructor.
	 */
	public BidragMellomlagring() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 * 
	 * @param bidragMellomlagringId DB ID.
	 * @param version DB version.
	 */
	public BidragMellomlagring(Long bidragMellomlagringId, long version) {
		this.bidragMellomlagringId = bidragMellomlagringId;
		setVersion(version);
	}
	
	/**
	 * Checks if an id is a BidragMellomlagringId
	 * 
	 * @param id The id to check.
	 * @return True if the id is a BidragMellomlagringId, false otherwise.
	 */
	public static boolean isBidragMellomLagringId(Long id) {
		return id.toString().startsWith(Integer.toString(ID_PREFIX)) && id.toString().length() == ID_WITH_PREFIX_LENGTH;
	}
	
	/**
	 * Removes the BidragMellomlagring prefix from an id.
	 * 
	 * @param idWithPrefix The id.
	 * @return The id without prefix.
	 */
	public static Long removePrefixFromId(Long idWithPrefix) {
		String id = idWithPrefix.toString();
		return Long.valueOf(id.substring(ID_PREFIX.toString().length()));
	}
	
	/**
	 * Adds the BidragMellomlagring prefix and necessary zero padding to an id.
	 * 
	 * @return The id with prefix.
	 */
	public Long getIdWithPrefix() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ID_PREFIX);
		stringBuilder.append(
				Strings.padStart(getBidragMellomlagringId().toString(), ID_WITH_PREFIX_LENGTH - ID_PREFIX_LENGTH, '0'));
		return Long.valueOf(stringBuilder.toString());
	}
	
	/**
	 * Getter for the avsenderFnr property.
	 *
	 * @return the avsenderFnr
	 */
	public String getAvsenderFnr() {
		return avsenderFnr;
	}

	/**
	 * Setter for the avsenderFnr property.
	 *
	 * @param avsenderFnr the avsenderFnr to set
	 */
	public void setAvsenderFnr(String avsenderFnr) {
		this.avsenderFnr = avsenderFnr;
	}

	/**
	 * Getter for the mottattDato property.
	 *
	 * @return the mottattDato
	 */
	public Date getMottattDato() {
		if (mottattDato != null) {
			return new Date(mottattDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the mottattDato property.
	 *
	 * @param mottattDato the mottattDato to set
	 */
	public void setMottattDato(Date mottattDato) {
		if (mottattDato != null) {
			this.mottattDato = new Date(mottattDato.getTime());
		} else {
			this.mottattDato = null;
		}
	}

	/**
	 * Getter for the status property.
	 *
	 * @return the status
	 */
	public BidragMellomlagringStatus getStatus() {
		return status;
	}

	/**
	 * Setter for the status property.
	 *
	 * @param status the status to set
	 */
	public void setStatus(BidragMellomlagringStatus status) {
		this.status = status;
	}

	/**
	 * Getter for the bidragMellomLagringId property.
	 *
	 * @return the bidragMellomLagringId
	 */
	public Long getBidragMellomlagringId() {
		return bidragMellomlagringId;
	}

	/**
	 * Add a BidragMellomlagringDokument to the BidragMellomlagringDokument Set.
	 * 
	 * @param bidragMellomlagringDokument The BidragMellomlagringDokument to add.
	 */
	public void addBidragMellomlagringDokument(BidragMellomlagringDokument bidragMellomlagringDokument) {
		if (bidragMellomlagringDokument != null) {
			this.bidragMellomlagringDokuments.add(bidragMellomlagringDokument);
			bidragMellomlagringDokument.setBidragMellomlagring(this);
		}
	}
	
	/**
	 * Getter for the bidragMellomlagringDokuments property.
	 *
	 * @return the bidragMellomlagringDokuments
	 */
	public Set<BidragMellomlagringDokument> getBidragMellomlagringDokuments() {
		return Collections.unmodifiableSet(bidragMellomlagringDokuments);
	}
	
	/**
	 * Finds a BidragMellomlagringDokument by BidragMellomlagringDokumentType.
	 * 
	 * @param bidragMellomlagringDokumentType
	 * @return Set of BidragMellomlagringDokument that are of type bidragMellomlagringDokumentType
	 */
	public Set<BidragMellomlagringDokument> findBidragMellomlagringDokumentByType(
			final BidragMellomlagringDokumentType bidragMellomlagringDokumentType) {
		return bidragMellomlagringDokuments.stream()
				.filter(bidragMellomlagringDokument -> bidragMellomlagringDokument.getDokumentType() == bidragMellomlagringDokumentType)
				.collect(Collectors.toSet());
	}
}
