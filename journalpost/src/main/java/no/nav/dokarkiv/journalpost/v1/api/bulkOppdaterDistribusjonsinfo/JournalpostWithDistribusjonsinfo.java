package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import no.nav.dokarkiv.journalpost.v1.api.WithUtsendingsKanal;

import java.time.OffsetDateTime;


@Builder
@Getter
@AllArgsConstructor
public class JournalpostWithDistribusjonsinfo implements WithUtsendingsKanal {
	@Schema(description = "Setter status 'ekspedert' på journalposten. Dvs. at journalposten har blitt distribuert til bruker", required = true)
	private Boolean settStatusEkspedert;

	@Schema(description = "Kanalen som dokumentene på journalpost ble sendt via. Se https://confluence.adeo.no/display/BOA/Utsendingskanal for lovlige verdier", required = true)
	private String utsendingsKanal;

	@Schema(description = "Unik identifikasjon av forsendelsen som er distribuert", required = true)
	private Long forsendelseId;

	@Schema(description = "Unik identifikasjon av journalposten som skal oppdateres", required = true)
	private Long journalpostId;

	@Schema(description = "Dato/tid når forsendelse ble ekspedert. Påkrevd hvis settEkspedert er true")
	private OffsetDateTime ekspedertDato;

	@Schema(description = "Påkrevd hvis utsendingsKanal = \"S\" (sentral print), settes ellers ikke")
	private Postadresse postadresse;

	@Schema(description = "Påkrevd hvis utsendingsKanal = \"SDP\" (digital postkasse), settes ellers ikke")
	private DigitalPost digitalpostkasse;

	@Schema(description = "Påkrevd hvis utsendingsKanal = \"NAV_NO\"")
	private NavNoVarsel varsel;
}
