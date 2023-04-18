package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentInfo {
	@NotNull(message = "DokumentInfo mangler dokumentInfoId")
	@Schema(
			description = "ID til dokumentinfo-objektet i Joark",
			required = true,
			example = "485227498")
	private String dokumentInfoId;

	@Schema(
			description = """
					Kode som sier noe om dokumentets innhold og oppbygning.
					For inngående dokumenter kan brevkoden være en NAV-skjemaID f.eks. "NAV 04-01.04" eller en SED-id.
					Brevkode skal ikke settes for ustrukturert, uklassifisert dokumentasjon, f.eks. brukeropplastede vedlegg.
					""",
			example = "NAV 04-01.04"
	)
	private String brevkode;

	@Schema(
			description = """
					Tittel som beskriver dokumentet, for eksempel "Søknad om dagpenger ved permittering".
					Dokumentets tittel blir synlig i brukers journal på nav.no, samt i NAVs fagsystemer.
					""",
			example = "Søknad om dagpenger ved permittering"
	)
	private String tittel;

	@Hidden
	@Schema(
			description = """
					Skjuler dokumentet for brukeren i Pselv (pensjon selvbetjening) ved påloggingsnivå 3 (MinId).
					Feltet skal kun brukes av pensjonsområdet.
					""",
			example = "false"
	)
	private Boolean sensitivtPselv;
}
