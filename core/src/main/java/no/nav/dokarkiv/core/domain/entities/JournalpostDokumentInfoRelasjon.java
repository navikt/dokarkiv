package no.nav.dokarkiv.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import org.hibernate.annotations.Cascade;

import java.io.Serial;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.hibernate.annotations.CascadeType.DETACH;
import static org.hibernate.annotations.CascadeType.MERGE;
import static org.hibernate.annotations.CascadeType.PERSIST;

/**
 * Mange til mange relasjon mellom {@link Journalpost} og {@link DokumentInfo}
 */
@Entity
@Table(name = "T_JP_DOK_INFO_REL")
@Builder
@Getter
@Setter
@AllArgsConstructor
public class JournalpostDokumentInfoRelasjon extends AbstractPersistentVersionedDomainObjectWithKilde {

	@Serial
	private static final long serialVersionUID = -2512784564042004318L;
	private static final String JOURNALPOST_DOKUMENT_INFO_RELASJON_SEQUENCE = "journalpost_dokument_info_relasjon_seq";
	private static final String DATABASE_JOURNALPOST_DOKUMENT_INFO_RELASJON_SEQUENCE = "t_jp_dok_info_rel_seq";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = JOURNALPOST_DOKUMENT_INFO_RELASJON_SEQUENCE)
	@SequenceGenerator(name = JOURNALPOST_DOKUMENT_INFO_RELASJON_SEQUENCE,
			sequenceName = DATABASE_JOURNALPOST_DOKUMENT_INFO_RELASJON_SEQUENCE, allocationSize = 1)
	@Column(name = "jp_dok_info_rel_id", nullable = false)
	private Long journalpostDokumentInfoRelasjonId;

	@Embedded
	private JournalpostDokumentInfoRelasjonId embeddedId;

	@Column(name = "tilknyttet_av_navn", nullable = false, length = 50)
	private String tilknyttetAvNavn;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_tilkn_jp_som", nullable = false, length = 20)
	private TilknyttetJournalpostSomCode tilknyttetJournalpostSom;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_skjerming_type", length = 50)
	private SkjermingTypeCode skjermingType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dokument_info_id", nullable = false)
	@Cascade({PERSIST, MERGE, DETACH})
	private DokumentInfo dokumentInfo;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	@Column(name = "rekkefoelge", length = 20)
	private Integer rekkefoelge;

	/**
	 * Default constructor.
	 */
	public JournalpostDokumentInfoRelasjon() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param journalpostDokumentInfoRelasjonId DB-id for the instance.
	 * @param version                           DB-version for the instance.
	 */
	public JournalpostDokumentInfoRelasjon(Long journalpostDokumentInfoRelasjonId, long version) {
		this.journalpostDokumentInfoRelasjonId = journalpostDokumentInfoRelasjonId;
		setVersion(version);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return getJournalpostDokumentInfoRelasjonId();
	}

	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyStringNotBlank(tilknyttetAvNavn, "tilknyttetAvNavn");
		verifyFieldNotNull(tilknyttetJournalpostSom, "tilknyttetJournalpostSom");
		verifyFieldsForEndeligJournalforing();
	}

	private void verifyFieldsForEndeligJournalforing() {
		if (journalpost.hasEndeligJournalforingStatus()) {
			verifyFieldNotNull(dokumentInfo, "dokumentInfo");
		}
	}

	/**
	 * Checks if this relasjon is not yet persisted and points to an already
	 * persisted DokumentInfo.
	 *
	 * @return true if relasjon is new and DokumentInfo isn't, false otherwise.
	 */
	public boolean isNewRelasjonToExistingDokumentInfo() {
		return journalpostDokumentInfoRelasjonId == null
			   && dokumentInfo != null
			   && dokumentInfo.getDokumentInfoId() != null;
	}

	/**
	 * Checks if this JournalpostDokumentInfoRelasjon is hoveddokument.
	 *
	 * @return true if hoveddokument, false otherwise.
	 */
	public boolean isHoveddokument() {
		return tilknyttetJournalpostSom == TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
	}

	/**
	 * Checks if this JournalpostDokumentInfoRelasjon is vedlegg.
	 *
	 * @return true if vedlegg, false otherwise.
	 */
	public boolean isVedlegg() {
		return tilknyttetJournalpostSom == TilknyttetJournalpostSomCode.VEDLEGG;
	}

	public void setDokumentInfo(DokumentInfo dokumentInfo) {
		this.dokumentInfo = dokumentInfo;
		if (dokumentInfo != null && dokumentInfo.getJournalpostRelasjoner() != null) {
			dokumentInfo.addJournalpostRelasjon(this);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof JournalpostDokumentInfoRelasjon other))
			return false;

		return journalpostDokumentInfoRelasjonId != null &&
			   journalpostDokumentInfoRelasjonId.equals(other.getJournalpostDokumentInfoRelasjonId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
