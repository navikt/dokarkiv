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
			value = "Identifikatoren til avsender/mottaker. Dette er normalt et fødselsnummer eller organisasjonsnummer, men valideres ikke.",
			required = false)
	private String id;

	@ApiModelProperty(
			value = "Identifikattype til avsender/mottaker.",
			required = false)
	private AvsenderMottakerIdType idType;

	@NotNull(message = "AvsenderMottaker mangler navn")
	@ApiModelProperty(
			value = "Navnet til avsender/mottaker.\nNavn på personbrukere skal lagres på formatet etternavn, fornavn mellomnavn",
			required = true)
	private String navn;

	@ApiModelProperty(
			value = "Landet forsendelsen er mottatt fra eller sendt til. Feltet skal i utgangspunktet kun settes dersom avsender eller mottaker er en institusjon med adresse i utlandet.",
			required = false)
	private String land;
}
