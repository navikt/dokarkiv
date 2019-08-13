package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class OppdaterDistribusjonsinfoRequest {
    @ApiModelProperty(value = "Setter status 'ekspedert' på journalposten. Dvs. at journalposten har blitt distribuert til bruker")
    private Boolean settStatusEkspedert;

    @ApiModelProperty(value = "Kanalen som dokumentene på journalpost ble sendt via. Se https://confluence.adeo.no/display/BOA/Utsendingskanal for lovlige verdier")
    private String utsendingsKanal;

}
