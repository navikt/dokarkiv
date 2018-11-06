package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.validator.BrukerValidator;
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
 * Domain entity class that represents Begrensning.
 * 
 * @author Ketill Fenne, Visma Consulting
 */
@Entity
@Table(name = "T_BEGRENSNING")
@Builder
@AllArgsConstructor
public class Begrensning extends AbstractPersistentVersionedDomainObjectWithKilde {

	/** ID used for serialization. */
	private static final long serialVersionUID = ***gammelt_fnr***36332183L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "t_begrensning_id_seq")
	@GenericGenerator(name = "t_begrensning_id_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
					  parameters = { @Parameter(name = "sequence_name", value = "t_begrensning_id_seq")})
	@Column(name = "begrensning_id", nullable = false)
	private Long begrensningId;

	@Enumerated(EnumType.STRING)
	@Column(name = "begrensning_type", length = 50, nullable = false)
	private BegrensningTypeCode begrensningType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journalpost_id")
	private Journalpost journalpost;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dokument_info_id")
	private DokumentInfo dokumentInfo;

	/**
	 * Default constructor.
	 */
	public Begrensning() {
	}


	public Long getId() {
		return begrensningId;
	}

	public BegrensningTypeCode getBegrensningType() {
		return begrensningType;
	}

	public void setBegrensningType(BegrensningTypeCode begrensningType) {
		this.begrensningType = begrensningType;
	}

	public Journalpost getJournalpost() {
		return journalpost;
	}

	public void setJournalpost(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

	public DokumentInfo getDokumentInfo() {
		return dokumentInfo;
	}

	public void setDokumentInfo(DokumentInfo dokumentInfo) {
		this.dokumentInfo = dokumentInfo;
	}
}
