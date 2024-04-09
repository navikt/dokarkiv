package no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg;

import io.swagger.v3.oas.annotations.media.Schema;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record LastOppVedleggRequest(
		@Schema(
				description = """
								Dokumentet som skal legges til som vedlegg
								""",
				requiredMode = REQUIRED
		)
		Dokument dokument
) {}
