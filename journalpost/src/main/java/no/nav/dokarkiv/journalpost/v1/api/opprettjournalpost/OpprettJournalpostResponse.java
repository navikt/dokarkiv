package no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpprettJournalpostResponse {

	@Schema(
			description = "JournalpostId som har blitt opprettet",
			required = true,
			example = "467010363"
	)
	private String journalpostId;

	/**
	 * @deprecated Skal ikke brukes lenger. Bruk journalpostferdigstilt istedenfor
	 */
	@Hidden
	@Schema(
			description = """
					Journalstatus for journalpost.
					* MIDLERTIDIG - hvis journalpost er opprettet
					* ENDELIG - hvis journalpost er opprett og endelig journalført

					 Feltet er deprekert og vil bli fjernet i fremtiden. Bruk journalpostferdigstilt i stedet.
					 """,
			required = true,
			example = "ENDELIG"
	)
	@Deprecated
	private String journalstatus;

	@Hidden
	@Schema(
			description = "Melding",
			example = "null"
	)
	private String melding;

	@Schema(
			description = "True eller False for om journalpost ble ferdigstilt",
			example = "true"
	)
	private Boolean journalpostferdigstilt;

	@Schema(
			description = "Dokumentene på journalposten."
	)
	private List<DokumentInfoId> dokumenter;
}
