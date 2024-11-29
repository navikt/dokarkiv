package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * Inneholder metadata om logiske vedlegg relatert til skanning.
 */
@Entity
@Table(name = "T_SKANNET_INNHOLD")
@Builder
@Getter
@Setter
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class SkannetInnhold extends AbstractPersistentVersionedDomainObjectWithKilde {
	public static final int VEDLEGG_INNHOLD_LENGTH = 550;
	private static final long serialVersionUID = 4377297136994040373L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skannetInnhold_seq")
	@GenericGenerator(name = "skannetInnhold_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "T_SKANNET_INNHOLD_SEQ"),
					@Parameter(name = "initial_value", value = "200000000")})
	@Column(name = "skannet_innhold_id", nullable = false)
	@ToString.Include
	private Long skannetInnholdId;

	@Column(name = "vedlegg_nr")
	private Integer vedleggNr;

	@Column(name = "vedlegg_innhold", length = VEDLEGG_INNHOLD_LENGTH)
	private String vedleggInnhold;

	@Column(name = "dokumenttypeid", length = 50)
	private String dokumenttypeid;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dokument_info_id", nullable = false)
	private DokumentInfo dokumentInfo;

	/**
	 * Default constructor.
	 */
	public SkannetInnhold() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param skannetInnholdId DB-id for the instance.
	 * @param version          DB-version for the instance.
	 */
	public SkannetInnhold(Long skannetInnholdId, long version) {
		this.skannetInnholdId = skannetInnholdId;
		setVersion(version);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return getSkannetInnholdId();
	}

	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyStringNotBlank(vedleggInnhold, "vedleggInnhold");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof SkannetInnhold other))
			return false;

		return skannetInnholdId != null &&
			   skannetInnholdId.equals(other.getSkannetInnholdId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
