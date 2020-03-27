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
            value = "Dokumentets tittel, f.eks. \"Søknad om dagpenger ved permittering\".\n" +
                    "Dokumentets tittel blir synlig i brukers journal på nav.no, samt i Gosys.",
            position = 1,
            example = "Søknad om dagpenger ved permittering"
    )
    private String tittel;

    @ApiModelProperty(
            value = "Typen dokument. Brevkoden sier noe om dokumentets innhold og oppbygning.\n" +
                    "For inngående dokumenter kan brevkoden være en NAV-skjemaID f.eks. \"NAV 04-01.04\" eller en SED-id.\n" +
                    "Brevkode skal ikke settes for ustrukturert, uklassifisert dokumentasjon, f.eks. brukeropplastede vedlegg.",
            position = 2,
            example = "NAV 04-01.04"
    )
    private String brevkode;

    @ApiModelProperty(
            value = "Dokumentets kategori, for eksempel SOK (søknad), SED eller FORVALTNINGSNOTAT.",
            hidden = true,
            example = "SOK"
    )
    private String dokumentKategori;

    @ApiModelProperty(
            position = 3
    )
    private List<DokumentVariant> dokumentvarianter = new ArrayList<>();
}
