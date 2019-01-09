package no.nav.dokarkiv.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
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
 * Domain object that represents the table used to keep track of the relation between
 * Journalposts and DokumentInfos
 *
 * @author Per Kristian Foss, Visma Sirius
 */
@Entity
@Table(name = "T_JP_DOK_INFO_REL")
@Builder
@AllArgsConstructor
public class JournalpostDokumentInfoRelasjon extends AbstractPersistentVersionedDomainObjectWithKilde {

	/**
	 * ID for serialization
	 */
	private static final long serialVersionUID = -***gammelt_fnr***42004318L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "journalpostDokumentInfoRelasjon_seq")
	@GenericGenerator(name = "journalpostDokumentInfoRelasjon_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "T_JP_DOK_INFO_REL_SEQ")})
	@Column(name = "jp_dok_info_rel_id", nullable = false)
	private Long journalpostDokumentInfoRelasjonId;

	@Column(name = "tilknyttet_av_navn", nullable = false)
	private String tilknyttetAvNavn;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_tilkn_jp_som", nullable = false)
	private TilknyttetJournalpostSomCode tilknyttetJournalpostSom;

	@ManyToOne
	@JoinColumn(name = "dokument_info_id", nullable = false)
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DETACH})
	private DokumentInfo dokumentInfo;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

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

	/**
	 * Getter for the journalpostDokumentInfoRelasjonId property.
	 *
	 * @return the journalpostDokumentInfoRelasjonId
	 */
	public Long getJournalpostDokumentInfoRelasjonId() {
		return journalpostDokumentInfoRelasjonId;
	}

	/**
	 * Getter for the tilknyttetAvNavn property.
	 *
	 * @return the tilknyttetAvNavn
	 */
	public String getTilknyttetAvNavn() {
		return tilknyttetAvNavn;
	}

	/**
	 * Setter for the tilknyttetAvNavn property.
	 *
	 * @param tilknyttetAvNavn the tilknyttetAvNavn to set
	 */
	public void setTilknyttetAvNavn(String tilknyttetAvNavn) {
		this.tilknyttetAvNavn = tilknyttetAvNavn;
	}

	/**
	 * Getter for the tilknyttetJournalpostSom property.
	 *
	 * @return the tilknyttetJournalpostSom
	 */
	public TilknyttetJournalpostSomCode getTilknyttetJournalpostSom() {
		return tilknyttetJournalpostSom;
	}

	/**
	 * Setter for the tilknyttetJournalpostSom property.
	 *
	 * @param tilknyttetJournalpostSom the tilknyttetJournalpostSom to set
	 */
	public void setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode tilknyttetJournalpostSom) {
		this.tilknyttetJournalpostSom = tilknyttetJournalpostSom;
	}

	/**
	 * Getter for the dokumentInfo property.
	 *
	 * @return the dokumentInfo
	 */
	public DokumentInfo getDokumentInfo() {
		return dokumentInfo;
	}

	/**
	 * Setter for the dokumentInfo property.
	 *
	 * @param dokumentInfo the dokumentInfo to set
	 */
	public void setDokumentInfo(DokumentInfo dokumentInfo) {
		this.dokumentInfo = dokumentInfo;
		if (dokumentInfo != null && dokumentInfo.getJournalpostRelasjoner() != null) {
			dokumentInfo.addJournalpostRelasjon(this);
		}
	}

	/**
	 * Getter for the journalpost property.
	 *
	 * @return the journalpost
	 */
	public Journalpost getJournalpost() {
		return journalpost;
	}

	/**
	 * Setter for the journalpost property.
	 *
	 * @param journalpost the journalpost to set
	 */
	void setJournalpost(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

}
