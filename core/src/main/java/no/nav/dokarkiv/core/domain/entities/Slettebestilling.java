package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_slettebestilling")
public class Slettebestilling extends AbstractPersistentVersionedDomainObjectWithKilde {

	public static final int SLETTEBESTILLING_BEGRUNNELSE_MAX_LENGTH = 512;
	private static final int LARGE_LENGTH = 512;
	private static final String SLETTEBESTILLING_SEQUENCE = "slettebestilling_seq";
	private static final String DATABASE_SLETTEBESTILLING_SEQUENCE = "t_slettebestilling_seq";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = SLETTEBESTILLING_SEQUENCE)
	@SequenceGenerator(name = SLETTEBESTILLING_SEQUENCE, sequenceName = DATABASE_SLETTEBESTILLING_SEQUENCE)
	@Column(name = "slettebestilling_id", nullable = false)
	private long slettebestillingId;

	@Enumerated(STRING)
	@Column(name = "k_slettebestilling_type", nullable = false)
	private SlettebestillingTypeCode slettebestillingType;

	@Enumerated(STRING)
	@Column(name = "k_slettebestilling_status", nullable = false)
	private SlettebestillingStatusCode slettebestillingStatus;

	@Enumerated(STRING)
	@Column(name = "k_slettebestilling_hjemmel", nullable = false)
	private SlettebestillingHjemmelCode slettebestillingHjemmel;

	@Enumerated(STRING)
	@Column(name = "k_slettebestilling_arsak", nullable = false)
	private SlettebestillingArsakCode slettebestillingArsak;

	@Column(name = "begrunnelse", length = SLETTEBESTILLING_BEGRUNNELSE_MAX_LENGTH)
	private String begrunnelse;

	@Column(name = "dokument_info_id")
	private Long dokumentInfoId;

	@Column(name = "sak_id")
	private Long sakId;

	@Column(name = "dato_utfores", nullable = false)
	private LocalDate datoUtfores;

	@Column(name = "dato_utfort")
	private LocalDateTime datoUtfort;

	@Column(name = "opprettet_av_navn", nullable = false, length = LARGE_LENGTH)
	private String opprettetAvNavn;

	@Column(name = "endret_av_navn", length = LARGE_LENGTH)
	private String endretAvNavn;

	@Override
	public Long getId() {
		return slettebestillingId;
	}

	public void setOpprettetAvOgChangestamp(String navn) {
		setChangeStamp(new ChangeStamp(navn));
	}
}
