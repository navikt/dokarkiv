package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;

import java.io.Serial;

import static jakarta.persistence.GenerationType.SEQUENCE;

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

	@Serial
	private static final long serialVersionUID = 2970255498067421424L;
	private static final String KRYSSREFERANSE_SEQUENCE = "kryssreferanse_seq";
	private static final String DATABASE_KRYSSREFERANSE_SEQUENCE = "t_kryssreferanse_seq";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = KRYSSREFERANSE_SEQUENCE)
	@SequenceGenerator(name = KRYSSREFERANSE_SEQUENCE, sequenceName = DATABASE_KRYSSREFERANSE_SEQUENCE, allocationSize = 1)
	@Column(name = "kryssreferanse_id", nullable = false)
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
