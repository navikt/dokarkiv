package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KopierJournalpostRequest {
	@Schema(
			description = """
					Unik id for forsendelsen som kan brukes til sporing gjennom verdikjeden. Eksempler på eksternReferanseId kan være en GUID, sykmeldingsId for sykmeldinger, Altinn ArchiveReference for Altinn-skjema eller SEDid for SED.
					                                                                                                                                                                      
					NB: Det er duplikatkontroll på eksternReferanseId. Dersom man sender inn en eksternReferanseId som allerede finnes i arkivet, vil tjenesten kaste feil (409 Conflict).
					""",
			example = "a0f480a3-8ab2-4c56-8c93-e53bb35bec2b",
			requiredMode = REQUIRED
	)
	private String eksternReferanseId;
}
