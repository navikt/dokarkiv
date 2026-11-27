package no.nav.dokarkiv.journalpost.v1.api.dokumentfil;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Metadata for initiering av direkte opplasting av en dokumentFil til objektlager")
public record InitierDokumentFilOpplastingRequest(
		@Schema(description = "Mediatype for dokumentet", example = "application/pdf", requiredMode = REQUIRED)
		String mediaType,
		@Schema(description = "Størrelse på dokumentet i bytes", example = "482133", requiredMode = REQUIRED)
		long stoerrelse,
		@Schema(description = "Base64-kodet big-endian CRC32C-sjekksum av dokumentet", example = "n03x6A==", requiredMode = REQUIRED)
		String crc32c
) {
}
