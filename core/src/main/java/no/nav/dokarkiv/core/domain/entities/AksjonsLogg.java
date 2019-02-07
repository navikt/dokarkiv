package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Entity
@Table(name = "T_AKSJONSLOGG")
@Builder
@Getter
@Setter
@Immutable
@NoArgsConstructor
@AllArgsConstructor
public class AksjonsLogg {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aksjonslogg_seq")
	@GenericGenerator(name = "aksjonslogg_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "t_aksjonslogg_seq")})
	@Column(name = "aksjonslogg_id", nullable = false, length = 11)
	private Long aksjonsloggId;

	@Column(name = "tidspunkt", nullable = false)
	private LocalDateTime tidspunkt;

	@Column(name = "applikasjon", length = 50, nullable = false)
	private String applikasjon;

	@Enumerated(EnumType.STRING)
	@Column(name = "aksjon", length = 50, nullable = false)
	private AksjonsTypeCode aksjon;

	@Column(name = "journalpost_id", length = 11)
	private Long journalpostId;

	@Column(name = "dokument_info_id", length = 11)
	private Long dokumentInfoId;

	@Column(name = "utfoert_av", length = 50, nullable = false)
	private String utfoertAv;

	@Column(name = "bruker", length = 50)
	private String bruker;

	@Column(name = "hjemmel", length = 50)
	private String hjemmel;

	@Column(name = "melding", length = 1000)
	private String melding;

	@OneToMany(mappedBy = "aksjonsLogg")
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	@Builder.Default
	private final Set<ArkivElementEndring> arkivElementEndringer = new HashSet<>();

	public String toString() {
		return String.format("AksjonsLogg(aksjonsloggId=%s, aksjon=%s)", aksjonsloggId, aksjon);
	}

}
