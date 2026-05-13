package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record SlettebestillingRequest(

		@Schema(
				description = """
					Hjemmel for sletting. En av følgende:
					 - POL - for sletting hjemlet i Personopplysningsloven
					 - ARK - for slettinger hjemlet i Arkivloven
					""",
				requiredMode = REQUIRED,
				example = "POL"
		)
		String hjemmel,

		@Schema(
				description = """
					Begrunnelse for hvorfor sletting gjøres. Maks 512 tegn. Kan f.eks være referanse til oppgave eller jira-sak.
					""",
				requiredMode = REQUIRED,
				example = "Jira: SLETT-1234"
		)
		String begrunnelse
) {
}
