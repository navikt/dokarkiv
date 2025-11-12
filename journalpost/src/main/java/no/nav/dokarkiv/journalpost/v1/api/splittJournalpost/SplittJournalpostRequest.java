package no.nav.dokarkiv.journalpost.v1.api.splittJournalpost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder(toBuilder = true)
public record SplittJournalpostRequest(
		@Schema(description = "Temaet som den nye journalposten skal ha. Dersom feltet ikke oppgis kopieres det fra den opprinnelige journalposten.", example = "DAG")
		String tema,

		@Schema(description = "Brukeren som dokumentene gjelder. Dersom feltet ikke oppgis kopieres det fra den opprinnelige journalposten.")
		Bruker bruker,

		@Schema(description = "Tittel på den nye journalposten. Dersom feltet ikke oppgis kopieres det fra den opprinnelige journalposten.")
		String tittel,

		@Schema(description = "Nav-enheten som den nye journalposten skal rutes til. Dersom feltet ikke oppgis blir journalførende Enhet nullet ut.", example = "9999")
		String journalfoerendeEnhet,

		@Schema(
				requiredMode = REQUIRED,
				description = """
					Unik id for forsendelsen som kan brukes til sporing gjennom verdikjeden. Eksempler på eksternReferanseId kan være en GUID, sykmeldingsId for sykmeldinger, Altinn ArchiveReference for Altinn-skjema eller SEDid for SED.
					
					NB: Det er duplikatkontroll på eksternReferanseId. Dersom man sender inn en eksternReferanseId som allerede finnes i arkivet, vil tjenesten kaste feil (409 Conflict).
					""",
				example = "a0f480a3-8ab2-4c56-8c93-e53bb35bec2b"
		)
		String eksternReferanseId,

		@Schema(
				requiredMode = REQUIRED,
				description = """
				    Liste over dokumenter som skal finnes på den nye journalposten. Alle dokumentInfoId som oppgis må være tilknyttet den opprinnelige journalposten.
				
				    Dokumentet som er øverst i listen blir hoveddokument på journalposten som opprettes.
				"""
		)
		List<SplittDokument> dokumenter
) {

	@Builder(toBuilder = true)
	public record SplittDokument(
			@Schema(requiredMode = REQUIRED, description = "DokumentInfoId til dokumentet som skal kopieres til den nye journalposten", example = "123456789")
			long dokumentInfoId,

			@Schema(requiredMode = REQUIRED,
					description = """
					  - True betyr at dokumentet skal kopieres uendret med til den nye journalposten. Originaldokumentet videreføres da som knyttet til den nye journalposten.
					  - False betyr at det lastes opp en modifisert versjon av dokumentet.
					""")
			Boolean kopierUtenEndringer,

			List<DokumentVariant> dokumentvarianter
	) {
		public SplittDokument {
			dokumentvarianter = dokumentvarianter == null ? List.of() : dokumentvarianter;
		}
	}
}
