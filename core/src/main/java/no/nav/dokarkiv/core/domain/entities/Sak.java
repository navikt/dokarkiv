package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.Date;

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

	@Enumerated(EnumType.STRING)
	@Column(name = "k_sak_status", length = 40)
	private SakStatusCode sakStatus;

	@Column(name = "endret_av", nullable = false, length = 40)
	private String endretAv;

	@Column(name = "dato_endret")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datoEndret;

	@Column(name = "dato_avsluttet")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datoAvsluttet;

	@Column(name = "endret_kilde_navn", length = 40)
	private String endretAvKildeNavn;

	@Column(name = "avsluttet_av", length = 40)
	private String avsluttetAv;

	@Column(name = "avsluttet_kilde_navn", length = 40)
	private String avsluttetAvNavn;

	@Column(name = "dato_sak_opprettet", length = 40)
	private String datoSakOpprettet;

	@Column(name = "administrativ_enhet", length = 40)
	private String administrativEnhet;

	@Column(name = "sak_ansvarlig", length = 40)
	private String sakAnsvarlig;

	// Finnes ikke (enda):
	@Column(name = "k_kassasjon_status", length = 40)
	private String kassasjonStatus;

	@Column(name = "k_avlevering_status", length = 40)
	private String avleveringStatus;

}
