package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record SlettebestillingRequest(

		@Schema(
				description = """
					Type slette-bestilling. Må være en av DOKUMENT, DOKUMENTER_PA_SAK, eller SAK
					""",
				requiredMode = REQUIRED,
				example = "DOKUMENT"
		)
		SlettebestillingTypeCode slettebestillingType,

		@Schema(
				description = """
					DokumentInfoId for dokument som skal slettes. Påkrevd om slettebestillingtype = DOKUMENT. Ellers skal den ikke settes.
					""",
				example = "12345"
		)
		Long dokumentInfoId,

		@Schema(
				description = """
					Id for sak som skal slettes / sak som har dokumenter som skal slettes. Påkrevd om slettebestillingtype er SAK eller DOKUMENTER_PA_SAK. Ellers skal den ikke settes.
					""",
				example = "54321"
		)
		Long sakId,

		@Schema(
				description = """
					Hjemmel for sletting. POL for sletting hjemlet i Personopplysningsloven, ARK for slettinger hjemlet i Arkivloven.
					""",
				requiredMode = REQUIRED,
				example = "POL"
		)
		SlettebestillingHjemmelCode hjemmel,

		@Schema(
				description = """
					Årsak for sletting. BEVARINGSTID eller ENKELTSLETTING. 
					""",
				requiredMode = REQUIRED,
				example = "ENKELTSLETTING"
		)
		SlettebestillingArsakCode arsak,

		@Schema(
				description = """
					Begrunnelse for hvorfor slettingen gjøres. Maks 512 tegn.
					""",
				requiredMode = REQUIRED,
				example = "Dette dokumentet skal slettes fordi..."
		)
		String begrunnelse
) {
}
