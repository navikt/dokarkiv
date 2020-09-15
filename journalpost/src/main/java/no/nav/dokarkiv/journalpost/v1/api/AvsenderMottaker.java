package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvsenderMottaker {
    @ApiModelProperty(
            value = "Identifikatoren til avsender/mottaker. Dette er normalt et fødselsnummer eller organisasjonsnummer, men valideres ikke. Dersom det ønskes å nullstille denne verdien, kan den settes til en tom string.",
            example = "\"09071844797\""
    )
    private String id;

    @ApiModelProperty(
            value = "Angir hvilken type identifikator som er benyttet i AvsenderMottaker.id.\nPåkrevd dersom `id` er satt. " +
                    "* FNR\n" +
                    "* ORGNR\n" +
                    "* HPRNR\n" +
                    "* UTL_ORG",
            example = "FNR"
    )
    private AvsenderMottakerIdType idType;

    @ApiModelProperty(
            value = "Navnet til avsender/mottaker.\nNavn på personbrukere skal lagres på formatet etternavn, fornavn mellomnavn",
            example = "Hansen, Per"
    )
    private String navn;

    @ApiModelProperty(
            value = "Landet forsendelsen er mottatt fra eller sendt til. Feltet skal i utgangspunktet kun settes dersom avsender eller mottaker er en institusjon med adresse i utlandet.",
            hidden = true,
            example = "Norge"
    )
    private String land;
}
