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
					Dokumentets tittel, f.eks. 'Søknad om foreldrepenger ved fødsel' eller 'Legeerklæring'.
					Dokumentets tittel blir synlig i brukers journal på nav.no, samt i NAVs fagsystemer.
					""",
			example = "Søknad om dagpenger ved permittering"
	)
	private String tittel;

	@Schema(
			description = """
					Kode som sier noe om dokumentets innhold og oppbygning.

					For inngående dokumenter kan brevkoden være en NAV-skjemaID f.eks. 'NAV 14-05.09' eller en SED-id.

					Utgående dokumenter og notater bør ha brevkode, og verdien bestemmes av konsument. Bruk gjerne brevets intern kode i fagsystemet.
					Brevkode skal ikke settes for ustrukturert, uklassifisert dokumentasjon, f.eks. brukeropplastede vedlegg.
					""",
			example = "NAV 04-01.04"
	)
	private String brevkode;

	@Schema(
			description = """
					Dokumentets plassering i listen.
					     
					Første dokument i meldingen skal ha rekkefoelge=1 og blir tilknyttet journalposten som hoveddokument. Hvis rekkefoelge ikke er satt på første dokument i meldingen settes rekkefoelge=1. Kun ett dokument kan ha rekkefoelge=1.
					     
					Tjenesten verifiserer ikke at duplikater ikke forekommer. Dersom flere vedlegg har samme rekkefoelge, vil rekkefølgen på disse være vilkårlig.
					""",
			example = "1"
	)
	private Integer rekkefoelge;

	@Hidden
	@Schema(
			description = """
					Dokumentets kategori, for eksempel SOK (søknad), SED eller FORVALTNINGSNOTAT.
					NB: Feltet brukes av eldre verdikjeder, men trenger ikke å settes av de nyere.
					""",
			example = "SOK"
	)
	private String dokumentKategori;

	@Hidden
	@Schema(
			description = """
					Skjuler dokumentet for brukeren i Pselv (pensjon selvbetjening) ved påloggingsnivå 3 (MinId).
					Feltet skal kun brukes av pensjonsområdet.
					""",
			example = "false"
	)
	private Boolean sensitivtPselv;

	@Schema(
			description = "Alle variantene av et enkeltdokument som skal arkiveres."
	)
	private List<DokumentVariant> dokumentvarianter = new ArrayList<>();
}
