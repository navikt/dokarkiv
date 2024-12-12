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
public class Bruker {
    @NotNull(message = "Bruker mangler idType")
    @Schema(
            description = "Angir hvilken type identifikator som er benyttet i `bruker.id`",
            requiredMode = REQUIRED,
            example = "FNR"
    )
    private BrukerIdType idType;

    @NotNull(message = "Bruker mangler id")
    @Schema(
            description = "Brukerens fødselsnummer (11 siffer), aktørID (13 siffer) eller organisasjonsnummer (9 siffer)",
            requiredMode = REQUIRED,
            example = "01117400200"
    )
    private String id;
}
