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
			description = """
					Identifikatoren til avsender/mottaker. Normalt et fødselsnummer eller organisasjonsnummer.

					Påkrevd dersom `avsenderMottaker.idType` er satt
					""",
			example = "09071844797"
	)
	private String id;

	@Schema(
			description = """
					Angir hvilken type identifikator som er benyttet i avsenderMottaker.id.
					Påkrevd dersom `avsenderMottaker.id` er satt.
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
					     
					Det er ikke nødvendig å oppgi navn når idType=FNR. OpprettJournalpost vil da hente personens navn fra PDL.
					""",
			example = "Hansen, Per"
	)
	private String navn;

	@Hidden
	@Schema(
			description = "Landet forsendelsen er mottatt fra eller sendt til. Brukes kun dersom avsender eller mottaker er en institusjon med adresse i utlandet.",
			example = "Norge"
	)
	private String land;
}
