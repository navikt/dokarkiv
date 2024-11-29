package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OppdaterJournalpostResponse {

    @NotNull(message = "OppdaterJournalpostResponse mangler journalpostId")
    @Schema(
            description = "JournalpostId som har blitt oppdatert (og forsøkt endelig journalført)",
            requiredMode = REQUIRED,
            example = "467011764"
    )
    private String journalpostId;
}
