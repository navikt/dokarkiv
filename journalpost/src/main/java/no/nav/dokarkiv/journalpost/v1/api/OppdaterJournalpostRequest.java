package no.nav.dokarkiv.journalpost.v1.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OppdaterJournalpostRequest {

	@Schema(
			description = """
					Tittel som beskriver forsendelsen samlet, for eksempel "Søknad om dagpenger ved permittering"
					""",
			example = "Søknad om dagpenger ved permittering"
	)
	private String tittel;

	@Schema(
			description = """
					Temaet som forsendelsen tilhører, for eksempel “DAG” (Dagpenger).
					Tema er påkrevd dersom Sak oppgis.
					""",
			example = "DAG")
	private String tema;

	@Schema(
			description = """
					Behandlingstema for forsendelsen, for eksempel ab0001 (Ordinære dagpenger).
					Lovlige verdier finnes i i Felles Kodeverksløsning.
					""",
			example = "ab0001"
	)
	private String behandlingstema;

	@Schema(
			description = """
					Avsender eller mottaker av forsendelsen.
					Skal ikke settes for notater.
					"""
	)
	private AvsenderMottaker avsenderMottaker;

	@Schema(
			description = "Brukeren som forsendelsen gjelder."
	)
	private Bruker bruker;

	@Schema(
			description = """
					Saken i PSAK eller GSAK som dokumentene skal journalføres mot.
					NB: Dersom journalposten tilhører en fagsak i et fagsystem,
					må konsument selv sørge for å opprette en GSAK-sak med mapping til fagsaken. Alternativt kan fagsystemet benytte tjenesten knyttTilSak,
					som knytter journalposten til en fagsak eller generell sak.
					"""
	)
	private Sak sak;


	@Hidden
	@Schema(
			description = """
					NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen. Ved automatisk journalføring uten mennesker involvert skal enhet settes til "9999".
					""",
			example = "9999"
	)
	private String journalfoerendeEnhet;

	@Hidden
	@Schema(
			description = "Dato forsendelsen ble mottatt i retur. Feltet kan kun settes for utgående journalposter."
	)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date datoRetur;

	@Schema(
			description = """
					Brukes for å datere innholdet i hoveddokumentet.
					
					Skal kun brukes dersom innholdet i dokumentet har en annen datering enn tidspunktet for opprettelse av journalposten.
					""",
			example = "2023-02-22T10:58:53.470892300"
	)
	private LocalDateTime datoDokument;

	@Hidden
	@Schema(
			description = "Dato forsendelsen ble mottatt fra avsender. Feltet kan kun settes for inngående journalposter.",
			example = "2019-11-29")
	private Date datoMottatt;

	@Schema(
			description = """
					Gjør at journalposten med dokumenter vises til pålogget bruker på nav.no selv om standardregelsettet sier at journalposten og/eller dokumentene skal skjules.
					Dersom flagget ikke settes, er det standard-regelsettet som styrer innsyn.
					
					* VISES_MASKINELT_GODKJENT brukes når en maskinell prosess har avgjort at dokumentene kan vises til bruker på nav.no.
					* VISES_MANUELT_GODKJENT brukes når en NAV-ansatt har sett over og godkjent at dokumentet kan vises til bruker på nav.no.
					* SKJULES_FEILSENDT brukes når et dokument er sendt til feil bruker og derfor skal skjules på nav.no
					* SKJULES_BRUKERS_SIKKERHET brukes når et dokument skal skjules på nav.no av hensyn til brukers sikkerhet
					* SKJULES_BRUKERS_ONSKE brukes når et dokument skal skjules på nav.no fordi brukeren selv ønsker dette
					* BRUK_STANDARDREGLER brukes ved behov for å oppheve overstyringen av innsynsregler
					""",
			example = "BRUK_STANDARDREGLER",
			nullable = true
	)
	private String overstyrInnsynsregler;

	@ArraySchema(arraySchema = @Schema(
			description = """
					Fagsystemene som arkiverer kan legge til egne fagspesifikke attributter per journalpost. Disse er representert
					som et skjemaløst nøkkel-verdi-sett og valideres ikke ved arkivering. Et eksempel på et slikt sett kan være
					nøkkel: bucid og verdi: 12345.
					"""
	)
	)
	private List<Tilleggsopplysning> tilleggsopplysninger;

	@ArraySchema(arraySchema = @Schema(
			description = "Liste over dokumentene på journalposten der metadata skal endres"
	)
	)
	private List<DokumentInfo> dokumenter;
}
