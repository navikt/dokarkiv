package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Sak {
	@Schema(
			description = """
					* FAGSAK vil si at dokumentene tilhører en sak i et fagsystem. Dersom FAGSAK velges, må fagsakid og fagsaksystem oppgis.
					* GENERELL_SAK kan brukes for dokumenter som skal journalføres, men som ikke tilhører en konkret fagsak. Generell sak kan ses på som brukerens "mappe" på et gitt tema.
					* ARKIVSAK skal kun brukes etter avtale.
					""",
			example = "FAGSAK"
	)
	private Sakstype sakstype;

	@Schema(
			description = """
					Iden til fagsaken i fagsystemet (altså ikke applikasjonen SAK).
					Skal kun settes dersom sakstype = FAGSAK.
					""",
			example = "10695768"
	)
	private String fagsakId;

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

					Skal kun settes dersom sakstype = FAGSAK.
					""",
			example = "AO01"
	)
	private Fagsaksystem fagsaksystem;

	@Hidden
	@Schema(
			description = """
					Saksnummeret i PSAK eller GSAK (SAK). Må være et numerisk heltall.
					Skal kun settes dersom sakstype = ARKIVSAK.
					Feltet skal kun brukes etter avtale.
					"""
	)
	@Deprecated
	private String arkivsaksnummer;

	@Hidden
	@Schema(
			description = """
					Skal kun settes dersom sakstype = ARKIVSAK.
					Feltet skal kun brukes etter avtale. GSAK,PSAK
					"""
	)
	@Deprecated
	private Arkivsaksystem arkivsaksystem;
}
