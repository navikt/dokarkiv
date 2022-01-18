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
public class AvsenderMottaker {
	@Schema(
			description = "Identifikatoren til avsender/mottaker. Dette er normalt et fødselsnummer eller organisasjonsnummer, men valideres ikke. Dersom det ønskes å nullstille denne verdien, kan den settes til en tom string.",
			example = "09071844797"
	)
	private String id;

	@Schema(
			description = """
					Angir hvilken type identifikator som er benyttet i AvsenderMottaker.id.
					Påkrevd dersom `id` er satt.
					* FNR
					* ORGNR
					* HPRNR
					* UTL_ORG
					""",
			example = "FNR"
	)
	private AvsenderMottakerIdType idType;

	@Schema(
			description = """
					Navnet til avsender/mottaker.
					Navn på personbrukere skal lagres på formatet etternavn, fornavn mellomnavn
					""",
			example = "Hansen, Per"
	)
	private String navn;

	@Hidden
	@Schema(
			description = "Landet forsendelsen er mottatt fra eller sendt til. Feltet skal i utgangspunktet kun settes dersom avsender eller mottaker er en institusjon med adresse i utlandet.",
			example = "Norge"
	)
	private String land;
}
