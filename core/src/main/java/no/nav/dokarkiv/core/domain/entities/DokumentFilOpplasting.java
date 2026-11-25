package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.ARKIVERES;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.ARKIVERT;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.UNDER_OPPLASTING;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.LASTET_OPP;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "T_DOKUMENT_FIL_OPPLASTING")
public class DokumentFilOpplasting {

	@Id
	@Column(name = "dokument_fil_id", nullable = false, length = 128)
	@JdbcTypeCode(SqlTypes.CHAR)
	private UUID dokumentFilId;

	@Column(name = "ekstern_dokument_referanse_id", nullable = false, length = 512)
	private String eksternDokumentReferanseId;

	@Column(name = "sha256_sjekksum")
	private byte[] sha256Sjekksum;

	@Column(name = "crc32c_sjekksum", nullable = false)
	private byte[] crc32cSjekksum;

	@Column(name = "media_type", nullable = false, length = 128)
	private String mediaType;

	@Setter
	@Enumerated(EnumType.STRING)
	@Column(name = "tilstand", nullable = false, length = 128)
	private DokumentFilOpplastingTilstand tilstand;

	@Column(name = "opprettet_kilde_navn", nullable = false, length = 512)
	private String opprettetKildeNavn;

	@CreationTimestamp
	@Column(name = "dato_opprettet", nullable = false)
	private LocalDateTime datoOpprettet;

	@Column(name = "endret_kilde_navn", length = 512)
	private String endretKildeNavn;

	@UpdateTimestamp
	@Column(name = "dato_endret")
	private LocalDateTime datoEndret;

	@Column(name = "dato_klient_lease", nullable = false)
	private LocalDateTime datoKlientLease;

	@Column(name = "opplasting_url", length = 4000)
	private String opplastingUrl;

	@Version
	@Column(name = "versjon", nullable = false)
	private int versjon;

	@Column(name = "antall_bytes")
	private Long antallBytes;

	@Column(name = "dato_lastet_opp")
	private LocalDateTime datoLastetOpp;

	@Column(name = "dato_arkivert")
	private LocalDateTime datoArkivert;

	@Column(name = "forsok", nullable = false)
	private int forsok;

	public DokumentFilOpplasting(UUID dokumentFilId,
								 String eksternDokumentReferanseId,
								 String mediaType,
								 byte[] crc32cSjekksum,
								 String opprettetKildeNavn,
								 LocalDateTime datoKlientLease) {
		this(dokumentFilId, eksternDokumentReferanseId, mediaType, crc32cSjekksum, opprettetKildeNavn, datoKlientLease, null, null);
	}

	public DokumentFilOpplasting(UUID dokumentFilId,
								 String eksternDokumentReferanseId,
								 String mediaType,
								 byte[] crc32cSjekksum,
								 String opprettetKildeNavn,
								 LocalDateTime datoKlientLease,
								 String opplastingUrl,
								 Long antallBytes) {
		this.dokumentFilId = dokumentFilId;
		this.eksternDokumentReferanseId = eksternDokumentReferanseId;
		this.mediaType = mediaType;
		this.crc32cSjekksum = crc32cSjekksum;
		this.tilstand = UNDER_OPPLASTING;
		this.opprettetKildeNavn = opprettetKildeNavn;
		this.datoKlientLease = datoKlientLease;
		this.antallBytes = antallBytes;
		this.opplastingUrl = opplastingUrl;
	}

	public void markerLastetOpp(long antallBytes,
								byte[] sha256Sjekksum,
								LocalDateTime datoLastetOpp,
								String endretKildeNavn) {
		this.tilstand = LASTET_OPP;
		this.antallBytes = antallBytes;
		this.sha256Sjekksum = sha256Sjekksum;
		this.datoLastetOpp = datoLastetOpp;
		this.endretKildeNavn = endretKildeNavn;
	}

	public void markerArkiveres(String endretKildeNavn) {
		if (!erLastetOpp()) {
			throw new IllegalStateException(String.format("dokumentFilId=%s kan ikke arkiveres med tilstand=%s", dokumentFilId, tilstand));
		}
		this.tilstand = ARKIVERES;
		this.endretKildeNavn = endretKildeNavn;
	}

	public boolean harSammeCrc32cSjekksum(byte[] annenCrc32cSjekksum) {
		return Arrays.equals(this.crc32cSjekksum, annenCrc32cSjekksum);
	}

	public boolean erUnderOpplasting() {
		return this.tilstand == UNDER_OPPLASTING;
	}

	public boolean erLastetOpp() {
		return this.tilstand == LASTET_OPP;
	}

	public boolean erBruktAvJournalpost() {
		return this.tilstand == ARKIVERES || this.tilstand == ARKIVERT;
	}
}
