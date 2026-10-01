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
import no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.LASTER_OPP;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.LASTET_OPP;

/// Sporer opplasting av en dokumentFil til dokarkivmellomlager via LastOppDokumentfil-tjenesten.
/// Brukes til å håndtere idempotens basert på eksternDokumentReferanseId (Idempotency-Key).
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

	@Column(name = "sha256_sjekksum", nullable = false)
	private byte[] sha256Sjekksum;

	@Column(name = "crc32c_sjekksum")
	private byte[] crc32cSjekksum;

	@Column(name = "media_type", nullable = false, length = 128)
	private String mediaType;

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

	@Version
	@Column(name = "versjon", nullable = false)
	private int versjon;

	@Column(name = "antall_bytes")
	private Long antallBytes;

	@Column(name = "dato_lastet_opp")
	private LocalDateTime datoLastetOpp;

	@Column(name = "dato_arkivert")
	private LocalDateTime datoArkivert;

	public DokumentFilOpplasting(UUID dokumentFilId,
								 String eksternDokumentReferanseId,
								 String mediaType,
								 byte[] sha256Sjekksum,
								 String opprettetKildeNavn,
								 LocalDateTime datoKlientLease) {
		this.dokumentFilId = dokumentFilId;
		this.eksternDokumentReferanseId = eksternDokumentReferanseId;
		this.mediaType = mediaType;
		this.sha256Sjekksum = sha256Sjekksum;
		this.tilstand = LASTER_OPP;
		this.opprettetKildeNavn = opprettetKildeNavn;
		this.datoKlientLease = datoKlientLease;
	}

	/// Markerer opplastingen som ferdig etter at innhold er verifisert i dokarkivmellomlager.
	///
	/// @param antallBytes    antall bytes som ble lastet opp.
	/// @param crc32cSjekksum CRC32C sjekksum
	/// @param datoLastetOpp  tidspunktet opplastingen ble ferdigstilt.
	public void markerLastetOpp(long antallBytes,
								byte[] crc32cSjekksum,
								LocalDateTime datoLastetOpp,
								String endretKildeNavn) {
		this.tilstand = LASTET_OPP;
		this.antallBytes = antallBytes;
		this.crc32cSjekksum = crc32cSjekksum;
		this.datoLastetOpp = datoLastetOpp;
		this.endretKildeNavn = endretKildeNavn;
	}

	public boolean harSammeSha256Sjekksum(byte[] annenSha256Sjekksum) {
		return Arrays.equals(this.sha256Sjekksum, annenSha256Sjekksum);
	}

	public boolean erOpplastet() {
		return this.tilstand != LASTER_OPP;
	}

	public boolean erUnderOpplasting() {
		return this.tilstand == LASTER_OPP;
	}
}
