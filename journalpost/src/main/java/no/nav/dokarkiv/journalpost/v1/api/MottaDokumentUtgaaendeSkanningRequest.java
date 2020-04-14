package no.nav.dokarkiv.journalpost.v1.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class MottaDokumentUtgaaendeSkanningRequest {
    @ApiModelProperty(
            dataType = "Date",
            example = "2019-11-29")
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date datoMottatt;

    @NotNull(message = "MottaDokumentUtgaaendeSkanningRequest mangler mottakskanal")
    @ApiModelProperty(
            value = "Mottakskanal for dokument"
    )
    private String mottakskanal;

    @ApiModelProperty(
            value = "Liste med Tilleggsopplysninger"
    )
    private List<Tilleggsopplysning> tilleggsopplysninger;

    @ApiModelProperty(
            value = "navn på batch"
    )
    private String batchnavn;

    @ApiModelProperty(
            value = "liste av skannede dokumenter"
    )
    private List<DokumentVariant> dokumentvarianter;
}
