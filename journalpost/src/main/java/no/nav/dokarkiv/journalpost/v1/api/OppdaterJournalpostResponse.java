package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OppdaterJournalpostResponse {
    @NotNull(message = "OppdaterJournalpostResponse mangler journalpostId")
    @Schema(
            description = "JournalpostId som har blitt oppdatert (og forsøkt endelig journalført)",
            required = true,
            example = "467011764"
    )
    private String journalpostId;
}
