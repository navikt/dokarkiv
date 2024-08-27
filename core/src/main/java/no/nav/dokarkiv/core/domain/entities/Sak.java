package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.SakStatusCode;
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
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Inneholder metadata om sakstilknytningen til fagsystemet.
 */
@Entity
@Table(name = "SAK")
@Builder
@Data
@Immutable
@NoArgsConstructor
@AllArgsConstructor
public class Sak {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_sak")
	@GenericGenerator(name = "seq_sak", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "seq_sak")})
	@Column(name = "id", nullable = false, length = 11)
	private Long sakId;

	@Column(name = "tema", nullable = false, length = 40)
	private String tema;

	@Column(name = "applikasjon", length = 40)
	private String applikasjon;

	@Column(name = "fagsaknr", length = 40)
	private String fagsakNr;

	@Column(name = "aktoerid", length = 40)
	private String aktoerId;

	@Column(name = "orgnr", length = 9)
	private String orgnr;

	@Column(name = "opprettet_av", nullable = false, length = 40)
	private String opprettetAv;

	@Column(name = "opprettet_tidspunkt", nullable = false)
	private LocalDateTime opprettetTidspunkt;

	@Column(name = "k_sak_status", length = 40)
	@Enumerated(EnumType.STRING)
	private SakStatusCode sakStatus;
}
