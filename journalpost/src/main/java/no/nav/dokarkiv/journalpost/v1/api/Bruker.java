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
public class Bruker {
    @NotNull(message = "Bruker mangler idType")
    @Schema(
            description = "Angir hvilken type identifikator som er benyttet i `bruker.id`",
            required = true,
            example = "FNR"
    )
    private BrukerIdType idType;

    @NotNull(message = "Bruker mangler id")
    @Schema(
            description = "Brukerens fødselsnummer (11 siffer), aktørID eller organisasjonsnummer (9 siffer)",
            required = true,
            example = "09071844797"
    )
    private String id;
}
