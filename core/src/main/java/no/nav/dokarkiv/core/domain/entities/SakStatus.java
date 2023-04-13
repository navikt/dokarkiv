package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.SakStatusCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serial;
import java.time.LocalDateTime;

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.NONE;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "T_SAK_STATUS")
@Builder(toBuilder = true)
public class SakStatus extends AbstractPersistentVersionedDomainObjectWithKilde {

	@Serial
	private static final long serialVersionUID = 9045863543269746293L;
	private static final String SAK_STATUS_SEQ = "T_SAK_STATUS_SEQ";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SAK_STATUS_SEQ)
	@SequenceGenerator(name = SAK_STATUS_SEQ, sequenceName = SAK_STATUS_SEQ)
	@Column(name = "sak_status_id", nullable = false)
	@Setter(NONE)
	private Long sakStatusId;

	@Enumerated(STRING)
	@Column(name = "k_sak_status", nullable = false, length = 40)
	private SakStatusCode status;

	@Column(name = "bruker_id", nullable = false, length = 11)
	private String brukerId;

	@Enumerated(STRING)
	@Column(name = "bruker_id_type", nullable = false, length = 40)
	private BrukerTypeCode brukerIdType;

	@Column(name = "k_fagomrade", nullable = false, length = 3)
	private FagomradeCode tema;

	@Column(name = "fagsaknr", length = 40)
	private String fagSakNr;

	@Column(name = "applikasjon", length = 40)
	private String applikasjon;

	@Column(name = "dato_avsluttet")
	private LocalDateTime datoAvsluttet;

	@Column(name = "avsluttet_av", length = 40)
	private String avsluttetAv;

	@Column(name = "dato_kassert")
	private LocalDateTime datoKassert;

	@Column(name = "dato_avlevert")
	private LocalDateTime datoAvlevert;

	@Column(name = "dato_sak_opprettet")
	private LocalDateTime datoSakOpprettet;

	@Column(name = "administrativ_enhet", length = 40)
	private String administrativEnhet;

	@Column(name = "sak_ansvarlig", length = 40)
	private String sakAnsvarlig;

	@Override
	public Long getId() {
		return sakStatusId;
	}
}
