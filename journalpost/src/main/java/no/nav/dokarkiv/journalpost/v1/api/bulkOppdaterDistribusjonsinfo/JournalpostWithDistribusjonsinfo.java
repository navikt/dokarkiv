package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import no.nav.dokarkiv.journalpost.v1.api.WithUtsendingsKanal;

import java.time.OffsetDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;


@Builder
@Getter
@AllArgsConstructor
public class JournalpostWithDistribusjonsinfo implements WithUtsendingsKanal {
	@Schema(description = "Setter status 'ekspedert' på journalposten. Dvs. at journalposten har blitt distribuert til bruker", requiredMode = REQUIRED)
	private Boolean settStatusEkspedert;

	@Schema(description = "Kanalen som dokumentene på journalpost ble sendt via. Se https://confluence.adeo.no/display/BOA/Utsendingskanal for lovlige verdier", requiredMode = REQUIRED)
	private String utsendingsKanal;

	@Schema(description = "Unik identifikasjon av forsendelsen som er distribuert", requiredMode = REQUIRED)
	private Long forsendelseId;

	@Schema(description = "Unik identifikasjon av journalposten som skal oppdateres", requiredMode = REQUIRED)
	private Long journalpostId;

	@Schema(description = "Dato/tid når forsendelse ble ekspedert. Påkrevd hvis settEkspedert er true")
	private OffsetDateTime ekspedertDato;

	@Schema(description = "Påkrevd hvis utsendingsKanal = \"S\" (sentral print), settes ellers ikke")
	private Postadresse postadresse;

	@Schema(description = "Påkrevd hvis utsendingsKanal = \"SDP\" (digital postkasse), settes ellers ikke")
	private DigitalPost digitalpostkasse;

	@Schema(description = "Påkrevd hvis utsendingsKanal = \"NAV_NO\" eller \"SDP\"")
	private Varsel varsel;
}
