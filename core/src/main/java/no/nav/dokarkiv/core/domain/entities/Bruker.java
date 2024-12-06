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
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.validator.BrukerValidator;

import java.io.Serial;

import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * Holder rede på hvilken bruker Journalposten gjelder.
 */
@Entity
@Table(name = "T_BRUKER")
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class Bruker extends AbstractPersistentVersionedDomainObjectWithKilde {

	@Serial
	private static final long serialVersionUID = -7460602621099426224L;
	private static final String BRUKER_SEQUENCE = "bruker_seq";
	private static final String DATABASE_BRUKER_SEQUENCE = "t_bruker_seq";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = BRUKER_SEQUENCE)
	@SequenceGenerator(name = BRUKER_SEQUENCE, sequenceName = DATABASE_BRUKER_SEQUENCE, initialValue = 200000000, allocationSize = 1)
	@Column(name = "brukerinfo_id", nullable = false)
	private Long brukerInfoId;

	@Column(name = "bruker_id", length = 11, nullable = false)
	private String brukerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_bruker_t", nullable = false, length = 20)
	private BrukerTypeCode brukerType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	/**
	 * Default constructor.
	 */
	public Bruker() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param brukerInfoId DB-id for the instance.
	 * @param version      DB-version for the instance.
	 */
	public Bruker(Long brukerInfoId, long version) {
		this.brukerInfoId = brukerInfoId;
		setVersion(version);
	}

	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyStringNotBlank(brukerId, "brukerId");
		verifyFieldNotNull(brukerType, "brukerType");
		validateBrukerId();
	}

	private void validateBrukerId() {
		BrukerValidator.validate(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return getBrukerInfoId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof Bruker other))
			return false;

		return brukerInfoId != null &&
			   brukerInfoId.equals(other.getBrukerInfoId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
