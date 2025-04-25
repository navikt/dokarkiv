package no.nav.dokarkiv.journalpost.v1.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class MottaDokumentUtgaaendeSkanningRequest {

    @Schema(
            description = "Dato batch ble mottatt",
            example = "2019-11-29")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate datoMottatt;

    @Schema(
            description = "Mottakskanal for dokument"
    )
    private String mottakskanal;

    @Schema(
            description = "Liste med tilleggsopplysninger"
    )
    private List<Tilleggsopplysning> tilleggsopplysninger;

    @Schema(
            description = "Navn på batch"
    )
    private String batchnavn;

    @Schema(
            description = "Liste av skannede dokumenter"
    )
    private List<DokumentVariant> dokumentvarianter;

    @Schema(
            description = "Ekstern referanse for journalpost"
    )
    private String eksternReferanseId;
}
