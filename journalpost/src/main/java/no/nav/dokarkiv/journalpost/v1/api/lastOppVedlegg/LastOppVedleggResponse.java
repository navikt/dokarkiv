package no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record LastOppVedleggResponse(
		@Schema(
				description = "Id til et dokumentInfo-objekt som peker på det arkiverte dokumentet.",
				requiredMode = REQUIRED
		)
		String dokumentInfoId
) {
}
