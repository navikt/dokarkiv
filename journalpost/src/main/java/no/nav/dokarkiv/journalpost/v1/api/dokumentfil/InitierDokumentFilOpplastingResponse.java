package no.nav.dokarkiv.journalpost.v1.api.dokumentfil;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.journalpost.v1.services.InitierDokumentFilOpplastingResult;
import no.nav.dokarkiv.journalpost.v1.services.LastOppDokumentFilResult;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Builder
@Schema(description = "Metadata og kortlivet tilgang for direkte opplasting av en dokumentfil")
public record InitierDokumentFilOpplastingResponse(
		@Schema(description = "UUID v7 for dokumentfilen", example = "01a086a4-7cfa-7f7f-a19a-1339a4028587")
		UUID dokumentFilId,
		@Schema(description = "Idempotency-Key fra requesten", example = "41ff35f0-dae9-41f8-80b0-4137592c16ca")
		String eksternDokumentReferanseId,
		@Schema(description = "Mediatype for dokumentet", example = "application/pdf")
		String mediaType,
		@Schema(description = "Base64-kodet big-endian CRC32C-sjekksum", example = "n03x6A==")
		String crc32c,
		@Schema(description = "Størrelse på dokumentet i antall bytes", example = "48867924")
		long stoerrelse,
		@Schema(description = "Tilstand for dokumentopplastingen", example = "UNDER_OPPLASTING")
		DokumentFilTilstand tilstand,
		@Schema(description = "Opprettelsestid i Europe/Oslo", example = "2026-11-03T15:20:00.000")
		LocalDateTime datoOpprettet,
		@Schema(description = "Siste endringstid i Europe/Oslo", example = "2026-11-03T15:20:00.000")
		LocalDateTime datoSistEndret,
		Opplasting opplasting
) {
	@Schema(description = "Kortlivet direkteopplasting")
	public record Opplasting(
			@Schema(description = "Signert PUT-URI til objektlagring")
			URI url,
			@Schema(description = "Utløpstid i Europe/Oslo. Standard varighet er 1 time.", example = "2026-11-03T16:20:00.000")
			LocalDateTime utloeper
	) {

	}

	public static InitierDokumentFilOpplastingResponse fra(InitierDokumentFilOpplastingResult result) {
		return InitierDokumentFilOpplastingResponse.builder()
				.dokumentFilId(result.dokumentFilId())
				.eksternDokumentReferanseId(result.eksternDokumentReferanseId())
				.mediaType(result.mediaType())
				.crc32c(Base64.getEncoder().encodeToString(result.crc32c()))
				.stoerrelse(result.stoerrelse())
				.tilstand(result.tilstand())
				.datoOpprettet(result.datoOpprettet())
				.datoSistEndret(result.datoSistEndret())
				.opplasting(new Opplasting(result.opplastingUrl(), result.opplastingUtloeper()))
				.build();
	}
}
