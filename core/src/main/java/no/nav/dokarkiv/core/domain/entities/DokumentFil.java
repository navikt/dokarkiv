package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Arrays;

/**
 * Domain object representing the table used to store files.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Entity
@Table(name = "T_DOKUMENT_FIL")
public class DokumentFil extends AbstractPersistentVersionedDomainObjectWithKilde {

	/**
	 * Named Parameter
	 */
	public static final String NP_FIL_UUID = "filUuid";
	/**
	 * Named Query
	 */
	public static final String NQ_FIND_BY_FIL_UUID = "DokumentFil.findDokumentFilByFilUuid";

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = ***gammelt_fnr***42760135L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dokumentFil_seq")
	@GenericGenerator(name = "dokumentFil_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "T_DOKUMENT_FIL_SEQ")})
	@Column(name = "dokument_fil_id", nullable = false)
	private Long id;

	@Column(name = "fil_uuid", nullable = false, length = 36)
	private String filUuid;

	@Column(name = "fil", nullable = false)
	@Lob
	private byte[] fil;

	/**
	 * Constructs a new DokumentFil.
	 */
	public DokumentFil() {
	}

	/**
	 * Constructs a new DokumentFil.
	 *
	 * @param id      The database Id
	 * @param version The database version
	 */
	public DokumentFil(Long id, long version) {
		this.id = id;
		setVersion(version);
	}

	/**
	 * Constructs a new DokumentFil.
	 *
	 * @param filUuid The file identifier.
	 * @param fil     The actual file.
	 */
	public DokumentFil(String filUuid, byte[] fil) {
		this.filUuid = filUuid;
		if (fil != null) {
			this.fil = Arrays.copyOf(fil, fil.length);
		}
	}

	/**
	 * Getter for the id property.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Setter for the id property.
	 *
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * Getter for the fil property.
	 *
	 * @return the fil
	 */
	public byte[] getFil() {
		if (ArrayUtils.isEmpty(fil)) {
			return null;
		}
		return Arrays.copyOf(fil, fil.length);
	}

	/**
	 * Setter for the fil property.
	 *
	 * @param fil the fil to set
	 */
	public void setFil(byte[] fil) {
		if (ArrayUtils.isEmpty(fil)) {
			this.fil = null;
		} else {
			this.fil = Arrays.copyOf(fil, fil.length);
		}
	}

}
