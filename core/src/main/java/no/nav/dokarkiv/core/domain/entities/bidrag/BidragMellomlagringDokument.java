package no.nav.dokarkiv.core.domain.entities.bidrag;

import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObject;
import org.apache.commons.lang3.ArrayUtils;
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Arrays;

/**
 * Domain object representing temporary stored Bidrag documents.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Entity
@Table(name = "T_BIDRAG_MELLOMLAGRING_DOK")
public class BidragMellomlagringDokument extends AbstractPersistentVersionedDomainObject {

	/** Serialization UID */
	private static final long serialVersionUID = ***gammelt_fnr***62027348L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bidragMellomlagringDok_seq")
	@GenericGenerator(name = "bidragMellomlagringDok_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
					  parameters = { @Parameter(name = "sequence_name", value = "T_BIDRAG_MELLOMLAGRING_DOK_SEQ") })
	@Column(name = "bidrag_mellomlagring_dok_id", nullable = false)
	private Long bidragMellomlagringDokumentId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "dokument_type", nullable = false)
	private BidragMellomlagringDokumentType dokumentType;
	
	@Column(name = "dokument", nullable = false)
	@Lob
	private byte[] dokument;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bidrag_mellomlagring_id", nullable = false)
	private BidragMellomlagring bidragMellomlagring;

	/**
	 * Constructs a new BidragMellomlagringDokument.
	 */
	public BidragMellomlagringDokument() {
	}
	
	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 * 
	 * @param bidragMellomlagringDokumentId DB ID.
	 * @param version DB version.
	 */
	public BidragMellomlagringDokument(Long bidragMellomlagringDokumentId, long version) {
		this.bidragMellomlagringDokumentId = bidragMellomlagringDokumentId;
		setVersion(version);
	}

	/**
	 * Getter for the dokumentType property.
	 *
	 * @return the dokumentType
	 */
	public BidragMellomlagringDokumentType getDokumentType() {
		return dokumentType;
	}

	/**
	 * Setter for the dokumentType property.
	 *
	 * @param dokumentType the dokumentType to set
	 */
	public void setDokumentType(BidragMellomlagringDokumentType dokumentType) {
		this.dokumentType = dokumentType;
	}

	/**
	 * Getter for the dokument property.
	 *
	 * @return the dokument
	 */
	public byte[] getDokument() {
		if (!ArrayUtils.isEmpty(dokument)) {
			return Arrays.copyOf(dokument, dokument.length);
		}
		return null;
	}

	/**
	 * Setter for the dokument property.
	 *
	 * @param dokument the dokument to set
	 */
	public void setDokument(byte[] dokument) {
		if (!ArrayUtils.isEmpty(dokument)) {
			this.dokument = Arrays.copyOf(dokument, dokument.length);
		} else {
			this.dokument = null;
		}
	}

	/**
	 * Getter for the bidragMellomlagringDokumentId property.
	 *
	 * @return the bidragMellomlagringDokumentId
	 */
	public Long getBidragMellomlagringDokumentId() {
		return bidragMellomlagringDokumentId;
	}

	/**
	 * Getter for the bidragMellomlagring property.
	 *
	 * @return the bidragMellomlagring
	 */
	public BidragMellomlagring getBidragMellomlagring() {
		return bidragMellomlagring;
	}

	/**
	 * Setter for the bidragMellomlagring property.
	 *
	 * @param bidragMellomlagring the bidragMellomlagring to set
	 */
	void setBidragMellomlagring(BidragMellomlagring bidragMellomlagring) {
		this.bidragMellomlagring = bidragMellomlagring;
	}
	
}
