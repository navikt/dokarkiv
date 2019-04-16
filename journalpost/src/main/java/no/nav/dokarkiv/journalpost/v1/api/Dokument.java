package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
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

	@ApiModelProperty(
			value = "Dokumentets tittel, f.eks. \"Søknad om foreldrepenger ved fødsel\" eller \"Legeerklæring\".\n" +
					"Dokumentets tittel blir synlig i brukers journal på nav.no, samt i Gosys.",
			required = false,
			example = "Søknad om foreldrepenger ved fødsel"
	)
	private String tittel;

	@ApiModelProperty(
			value = "Typen dokument. Brevkoden sier noe om dokumentets innhold og oppbygning.\n" +
					"For inngående dokumenter kan brevkoden være en NAV-skjemaID f.eks. \"NAV 14-05.09\" eller en SED-id.\n" +
					"Brevkode skal ikke settes for ustrukturert, uklassifisert dokumentasjon, f.eks. brukeropplastede vedlegg.",
			required = false,
			example = "NAV 14-05.09"
	)
	private String brevkode;

	@ApiModelProperty(
			value = "Dokumentets kategori, for eksempel SOK (søknad), SED eller FORVALTNINGSNOTAT.",
			required = false,
			example = "SOK"
	)
	private String dokumentKategori;

	@ApiModelProperty(
			value = "",
			required = true)
	private List<DokumentVariant> dokumentvarianter = new ArrayList<>();
}
