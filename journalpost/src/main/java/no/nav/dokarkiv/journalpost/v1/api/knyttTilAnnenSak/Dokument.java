package no.nav.dokarkiv.journalpost.v1.api.knyttTilAnnenSak;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record Dokument(
		@Schema(description = "ID til dokumentet", requiredMode = REQUIRED, example = "12345678")
		String dokumentInfoId) {
}
