package no.nav.dokarkiv.journalpost.v1.api.dokumentfil;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.journalpost.v1.services.LastOppDokumentFilResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentFilResponse {

	@Schema(description = "UUID v7 for den opplastede dokumentFilen", example = "01a086a4-7cfa-7f7f-a19a-1339a4028587", requiredMode = REQUIRED)
	private UUID dokumentFilId;

	@Schema(description = "Idempotency-Key fra header", example = "41ff35f0-dae9-41f8-80b0-4137592c16ca", requiredMode = REQUIRED)
	private String eksternDokumentReferanseId;

	@Schema(description = "Mediatype for dokumentet", example = APPLICATION_PDF_VALUE, requiredMode = REQUIRED)
	private String mediaType;

	@Schema(description = "Crc32c sjekksum av dokumentet, big-endian base64-kodet", example = "mihUuA==", requiredMode = REQUIRED)
	private byte[] crc32c;

	@Schema(description = "Sha256 sjekksum av dokumentet beregnet av dokarkiv, base64-kodet", example = "ldiZGMZRiYNtkuGx/ee3q62DuQLPUzMimcEc0rv9R4o=")
	private byte[] sha256;

	@Schema(description = "Størrelse på dokumentet i antall bytes", example = "48867924")
	private long stoerrelse;

	@Schema(description = "Hvilken tilstand dokumentopplastingen har", example = "LASTET_OPP", requiredMode = REQUIRED)
	private DokumentFilTilstand tilstand;

	@Schema(description = "Dato for oppretting. Lokaltid Europe/Oslo", example = "2026-09-25T10:58:53.470", requiredMode = REQUIRED)
	private LocalDateTime datoOpprettet;

	@Schema(description = "Dato for siste endring. Lokaltid Europe/Oslo", example = "2026-09-25T10:59:53.470", requiredMode = REQUIRED)
	private LocalDateTime datoSistEndret;

	public static DokumentFilResponse fra(LastOppDokumentFilResult lastOppDokumentFilResult) {
		return DokumentFilResponse.builder()
				.dokumentFilId(lastOppDokumentFilResult.dokumentFilId())
				.eksternDokumentReferanseId(lastOppDokumentFilResult.eksternDokumentReferanseId())
				.mediaType(lastOppDokumentFilResult.mediaType())
				.sha256(lastOppDokumentFilResult.sha256())
				.crc32c(lastOppDokumentFilResult.crc32c())
				.stoerrelse(lastOppDokumentFilResult.stoerrelse())
				.tilstand(lastOppDokumentFilResult.tilstand())
				.datoOpprettet(lastOppDokumentFilResult.datoOpprettet())
				.datoSistEndret(lastOppDokumentFilResult.datoSistEndret())
				.build();
	}
}
