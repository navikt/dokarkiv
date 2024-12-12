package no.nav.dokarkiv.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

import java.io.Serial;

import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * Inneholder metadata om arkivsaken (internt arkivbegrep).
 * Knytter {@link Journalpost} mot {@link Sak} tilhørende fagsystem.
 */
@Entity
@Table(name = "T_SAKSRELASJON")
@Builder
@Getter
@Setter
@AllArgsConstructor
public class Saksrelasjon extends AbstractPersistentVersionedDomainObjectWithKilde {

	@Serial
	private static final long serialVersionUID = 588843673270038569L;
	private static final String SAKSRELASJON_SEQUENCE = "saksrelasjon_seq";
	private static final String DATABASE_SAKSRELASJON_SEQUENCE = "t_saksrelasjon_seq";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = SAKSRELASJON_SEQUENCE)
	@SequenceGenerator(name = SAKSRELASJON_SEQUENCE, sequenceName = DATABASE_SAKSRELASJON_SEQUENCE, initialValue = 200000000, allocationSize = 1)
	@Column(name = "saksrelasjon_id", nullable = false)
	private Long saksrelasjonId;

	@Column(name = "sak_id", nullable = false)
	private Long sakId;

	@Column(name = "journalpost_id", nullable = false, insertable = false, updatable = false)
	private Long journalpostId;

	@Column(name = "feilregistrert")
	private Boolean feilregistrert;

	@Column(name = "endret_av_navn", length = 50)
	private String endretAvNavn;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_fagsystem", nullable = false, length = 20)
	private FagsystemCode fagsystem;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	/**
	 * Default constructor.
	 */
	public Saksrelasjon() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param saksrelasjonId DB-id for the instance.
	 * @param version        DB-version for the instance.
	 */
	public Saksrelasjon(Long saksrelasjonId, long version) {
		this.saksrelasjonId = saksrelasjonId;
		setVersion(version);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return getSaksrelasjonId();
	}

	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyMandatoryFieldsNotEndretAvNavn();
		if (saksrelasjonId != null) {
			verifyStringNotBlank(endretAvNavn, "endretAvNavn");
		}
	}

	public void verifyMandatoryFieldsNotEndretAvNavn() {
		verifyFieldNotNull(sakId, "sakId");
		verifyFieldNotNull(fagsystem, "fagsystem");
	}
}
