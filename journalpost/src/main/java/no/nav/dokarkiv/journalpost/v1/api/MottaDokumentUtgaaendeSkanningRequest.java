package no.nav.dokarkiv.journalpost.v1.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

    @ApiModelProperty(
            value = "nummer som trykkes på selve papirdokumentet under skanning, slik at dette skal være mulig å søke opp i ettertid"
    )
    private String endorsernr;

    @ApiModelProperty(
            value = "hvor dokumentet er mottatt fra"
    )
    private String mottattfra;

    @ApiModelProperty(
            value = "hvor dokumentet er mottatt i"
    )
    private String mottatti;

    @ApiModelProperty(
            value = "navn på batch"
    )
    private String batchnavn;

    @ApiModelProperty(
            value = "liste av skannede dokumenter"
    )
    private List<DokumentVariant> dokumentvarianter;
}
