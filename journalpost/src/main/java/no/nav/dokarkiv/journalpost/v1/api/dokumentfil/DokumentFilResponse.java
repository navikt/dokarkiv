package no.nav.dokarkiv.journalpost.v1.api.dokumentfil;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.journalpost.v1.services.LastOppDokumentFilResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentFilResponse {

	@Schema(description = "UUID v7 for den opplastede dokumentFilen", example = "01a086a4-7cfa-7f7f-a19a-1339a4028587")
	private UUID dokumentFilId;

	@Schema(description = "Idempotency-Key fra header", example = "41ff35f0-dae9-41f8-80b0-4137592c16ca")
	private String eksternDokumentReferanseId;

	@Schema(description = "Mediatype for dokumentet", example = APPLICATION_PDF_VALUE)
	private String mediaType;

	@Schema(description = "Sha256 sjekksum av dokumentet, base64-kodet", example = "ldiZGMZRiYNtkuGx/ee3q62DuQLPUzMimcEc0rv9R4o=")
	private byte[] sha256Sjekksum;

	@Schema(description = "Hvilken tilstand dokumentopplastingen har", example = "LASTET_OPP")
	private DokumentFilTilstand tilstand;

	@Schema(description = "Dato for oppretting. Lokaltid Europe/Oslo", example = "2026-09-25T10:58:53.470")
	private LocalDateTime datoOpprettet;

	@Schema(description = "Dato for siste endring. Lokaltid Europe/Oslo", example = "2026-09-25T10:59:53.470")
	private LocalDateTime datoSistEndret;

	public static DokumentFilResponse fra(LastOppDokumentFilResult lastOppDokumentFilResult) {
		return DokumentFilResponse.builder()
				.dokumentFilId(lastOppDokumentFilResult.dokumentFilId())
				.eksternDokumentReferanseId(lastOppDokumentFilResult.eksternDokumentReferanseId())
				.mediaType(lastOppDokumentFilResult.mediaType())
				.sha256Sjekksum(lastOppDokumentFilResult.sha256Sjekksum())
				.tilstand(lastOppDokumentFilResult.tilstand())
				.datoOpprettet(lastOppDokumentFilResult.datoOpprettet())
				.datoSistEndret(lastOppDokumentFilResult.datoSistEndret())
				.build();
	}
}
