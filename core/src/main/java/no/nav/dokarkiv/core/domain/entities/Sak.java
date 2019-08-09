package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Entity
@Table(name = "T_SAK")
@Builder
@Data
@Immutable
@NoArgsConstructor
@AllArgsConstructor
public class Sak {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sak_seq")
	@GenericGenerator(name = "sak_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "t_sak_seq")})
	@Column(name = "id", nullable = false, length = 11)
	private Long sakId;

	@Column(name = "tema", nullable = false)
	private String tema;

	@Column(name = "applikasjon")
	private String applikasjon;

	@Column(name = "fagsaknr")
	private String fagsakNr;

	@Column(name = "aktoerid")
	private String aktoerId;

	@Column(name = "orgnr")
	private String orgnr;

	@Column(name = "opprettet_av", nullable = false)
	private String opprettetAv;

	@Column(name = "opprettet_tidspunkt", nullable = false)
	private LocalDateTime opprettetTidspunkt;
}
