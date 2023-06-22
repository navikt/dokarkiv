package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;


@Builder
@Getter
@AllArgsConstructor
public class OppdaterDistribusjonsinfoRequest implements WithUtsendingsKanal {
    @Schema(
            description = "Setter status 'ekspedert' på journalposten. Dvs. at journalposten har blitt distribuert til bruker"
    )
    private Boolean settStatusEkspedert;

    @Schema(
            description = "Kanalen som dokumentene på journalpost ble sendt via. Se https://confluence.adeo.no/display/BOA/Utsendingskanal for lovlige verdier"
    )
    private String utsendingsKanal;
    @Schema(
            description = "Sett tidspunkt journalposten ble lest, om det ikke allerede er satt"
    )
    private OffsetDateTime datoLest;
    @Schema(
            description = "Settes til true hvis en tidligere ekspedert journalpost skal tilbakestilles i forbindelse med distribusjon i ny kanal (ny kanal angitt i utsendingskanal)"
    )
    private Boolean tilbakestillJournalpost;

}
