package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Domain entity class that represents Begrensning.
 * 
 * @author Ketill Fenne, Visma Consulting
 */
@Entity
@Table(name = "T_BEGRENSNING")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
	@Column(name = "begrensning_type", nullable = false, length = 50)
	private BegrensningTypeCode begrensningType;

	@Column(name = "journalpost_id")
	private Long journalpostId;

	@Column(name = "dokument_info_id")
	private Long dokumentInfoId;

	public Long getId() {
		return begrensningId;
	}
}
