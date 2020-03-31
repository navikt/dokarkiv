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
    private String endorsernr;
    private String mottattfra;
    private String mottatti;
    private String batchnavn;
    private List<DokumentVariant> dokumentvarianter;
}
