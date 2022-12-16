package no.nav.dokarkiv.core.domain.entities;

import lombok.Getter;
import lombok.Setter;
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
 * Seperat tabell for dokumentfiler. Metadata og dokumenter er adskilt.
 * fil_uuid er er en indeks som FilDetaljer holder rede på.
 */
@Entity
@Table(name = "T_DOKUMENT_FIL")
@Getter
@Setter
public class DokumentFil extends AbstractPersistentVersionedDomainObjectWithKilde {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = 4404983937742760135L;

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
