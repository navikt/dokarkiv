package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.validator.BrukerValidator;
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
 * Holder rede på hvilken bruker Journalposten gjelder.
 */
@Entity
@Table(name = "T_BRUKER")
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class Bruker extends AbstractPersistentVersionedDomainObjectWithKilde {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = -7460602621099426224L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brukerInfo_seq")
	@GenericGenerator(name = "brukerInfo_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "T_BRUKER_SEQ"),
					@Parameter(name = "initial_value", value = "200000000")})
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
