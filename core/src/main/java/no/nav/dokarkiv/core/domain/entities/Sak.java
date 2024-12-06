package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.AvleveringStatusCode;
import no.nav.dokarkiv.core.domain.codes.KassasjonStatusCode;
import no.nav.dokarkiv.core.domain.codes.SakStatusCode;

import java.time.LocalDateTime;
import java.util.Date;

import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * Inneholder metadata om sakstilknytningen til fagsystemet.
 */
@Entity
@Table(name = "SAK")
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sak {

	private static final String SAK_SEQUENCE = "seq_sak";
	private static final String DATABASE_SAK_SEQUENCE = "seq_sak";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = SAK_SEQUENCE)
	@SequenceGenerator(name = SAK_SEQUENCE, sequenceName = DATABASE_SAK_SEQUENCE, allocationSize = 1)
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

	@Enumerated(EnumType.STRING)
	@Column(name = "k_sak_status", length = 40)
	private SakStatusCode sakStatus;

	@Column(name = "endret_av", length = 40)
	private String endretAv;

	@Column(name = "dato_endret")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datoEndret;

	@Column(name = "dato_avsluttet")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datoAvsluttet;

	@Column(name = "endret_kilde_navn", length = 40)
	private String endretKildeNavn;

	@Column(name = "avsluttet_av", length = 40)
	private String avsluttetAv;

	@Column(name = "avsluttet_kilde_navn", length = 40)
	private String avsluttetKildeNavn;

	@Column(name = "dato_sak_opprettet", length = 40)
	private Date datoSakOpprettet;

	@Column(name = "administrativ_enhet", length = 40)
	private String administrativEnhet;

	@Column(name = "sak_ansvarlig", length = 40)
	private String sakAnsvarlig;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_kassasjon_status", length = 40)
	private KassasjonStatusCode kassasjonStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_avlevering_status", length = 40)
	private AvleveringStatusCode avleveringStatus;

}
