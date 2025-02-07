package no.nav.dokarkiv.journalpost.v1.api.sak;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvsluttSakRequest {

	@Schema(description = "Tre-bokstavers kode for tema/fagområde på saken som skal avsluttes.",
			name = "tema",
			example = "MED",
			requiredMode = REQUIRED)
	String tema;

	@Schema(description = "Saksnummer i fagsystemet",
			name = "fagsakId",
			example = "2403479",
			requiredMode = REQUIRED)
	String fagsakId;

	@Schema(
			description = """
					Fagsystemet som saken behandles i. Lovlige verdier er
					* FS38 (Melosys)
					* FS36 (Foreldrepengeløsningen)
					* UFM (Unntak fra medlemskap)
					* AO01 (Arena)
					* AO11 (Grisen)
					* IT01 (Infotrygd)
					* OEBS
					* PP01
					* K9
					* BISYS
					* BA (Barnetrygd)
					* EF (Enslig forsørger)
					* KONT (Kontantstøtte)
					* SUPSTONAD (Supplerende Stønad)
					* OMSORGSPENGER
					* HJELPEMIDLER
					* BARNEBRILLER
					* EY (Etterlatteytelser)
					* KELVIN
					* DAGPENGER
					* KOMPYS
					* ARGUS
					* NEESSI
					* TILLEGGSSTONADER
					* ARBEIDSOPPFOLGING
					* TILTAKSPENGER
					* TILTAKSADMINISTRASJON
					* FIA

					Skal kun settes dersom sakstype = FAGSAK.
					""",
			example = "AO01", requiredMode = REQUIRED
	)
	String fagsaksystem;

	@Schema(description = "Bruker som saken som skal avsluttes tilhører.",
			name = "bruker",
			requiredMode = REQUIRED )
	Bruker bruker;

	@Schema(description = "Tidspunktet når saken ble opprettet i fagsystemet.",
			name = "opprettetDato",
			requiredMode = REQUIRED )
	LocalDateTime opprettetDato;

	@Schema(description = "Tidspunktet når saken ble avsluttet. Hvis ikke satt så settes tidspunktet til tidspunktet når tjenesten kalles. Kan ikke være frem i tid",
			name = "avsluttetDato",
			requiredMode = NOT_REQUIRED )
	LocalDateTime avsluttetDato;

	@Schema(description = "Navn på enhet som er ansvarlig for saken.",
			name = "administrativEnhet",
			requiredMode = REQUIRED )
	String administrativEnhet;

	@Schema(description = "Ansvarlig for fagsak. Hvis ikke satt så oppgis administrativ enhet som sakansvarlig",
			name = "sakAnsvarlig",
			requiredMode = NOT_REQUIRED )
	String sakAnsvarlig;
}
