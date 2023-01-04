package no.nav.dokarkiv.core.domain.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Inneholder metadata om kryssreferanser.
 */
@Entity
@Table(name = "T_KRYSSREFERANSE")
@Builder
@Getter
@Setter
@AllArgsConstructor
public class Kryssreferanse extends AbstractPersistentVersionedDomainObjectWithKilde {

	/**
	 * ID for serialization
	 */
	private static final long serialVersionUID = 2970255498067421424L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kryssreferanse_seq")
	@GenericGenerator(name = "kryssreferanse_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "T_KRYSSREFERANSE_SEQ")})
	@Column(name = "kryssreferanse_id", nullable = false)
	@Setter(AccessLevel.NONE)
	private Long kryssreferanseId;

	@Column(name = "referanse_id", nullable = false, length = 20)
	private String referanseId;

	@Column(name = "referanse_nr")
	private Long referanseNr;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_referanse_t", nullable = false, length = 20)
	private ReferanseTypeCode referanseType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	/**
	 * Default constructor.
	 */
	public Kryssreferanse() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param kryssreferanseId DB-id for the instance.
	 * @param version          DB-version for the instance.
	 */
	public Kryssreferanse(Long kryssreferanseId, long version) {
		this.kryssreferanseId = kryssreferanseId;
		setVersion(version);
	}

	/**
	 * {@inheritDoc}
	 */
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof Kryssreferanse other))
			return false;

		return kryssreferanseId != null &&
			   kryssreferanseId.equals(other.getKryssreferanseId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
