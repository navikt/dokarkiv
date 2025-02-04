package no.nav.dokarkiv.journalpost.v1.api.sak;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GjenaapneSakRequest {

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

}
