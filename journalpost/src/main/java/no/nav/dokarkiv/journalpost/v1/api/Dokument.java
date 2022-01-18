package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Dokument {

	@Schema(
			description = """
					Dokumentets tittel, f.eks. "Søknad om dagpenger ved permittering".
					Dokumentets tittel blir synlig i brukers journal på nav.no, samt i Gosys.
					""",
			example = "Søknad om dagpenger ved permittering"
	)
	private String tittel;

	@Schema(
			description = """
					Typen dokument. Brevkoden sier noe om dokumentets innhold og oppbygning.
					For inngående dokumenter kan brevkoden være en NAV-skjemaID f.eks. "NAV 04-01.04" eller en SED-id.
					Brevkode skal ikke settes for ustrukturert, uklassifisert dokumentasjon, f.eks. brukeropplastede vedlegg.""",
			example = "NAV 04-01.04"
	)
	private String brevkode;

	@Hidden
	@Schema(
			description = "Dokumentets kategori, for eksempel SOK (søknad), SED eller FORVALTNINGSNOTAT.",
			example = "SOK"
	)
	private String dokumentKategori;

	@Schema
	private List<DokumentVariant> dokumentvarianter = new ArrayList<>();
}
