package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

/*
 * Deprecated er midertidlig lagt til for migrering fra ondemand to joark, gjelder sak MMA-5140.
 * */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FerdigstillJournalpostRequest {

	@Schema(
			description = """
					NAV-enheten som personen som utfører journalføring jobber for. Ved automatisk journalføring uten mennesker involvert, skal enhet settes til "9999".
					""",
			required = true,
			example = "9999"
	)
	private String journalfoerendeEnhet;

	@Deprecated
	@Schema(
			description = "Navn på saksbehandler eller system som journalførte.",
			example = "srvbruker"
	)
	private String journalfortAvNavn;

	@Deprecated
	@Schema(
			description = "Navn på saksbehandler eller system som opprettet journalposten.",
			example = "srvbruker"
	)
	private String opprettetAvNavn;

	@Deprecated
	@Schema(
			description = "Dato for ferdigstilling av dokumentet i Infotrygd.",
			example = "2020-02-20"
	)
	private Date datoJournal;

	@Deprecated
	@Schema(
			description = "Datoen journalposten ble plukket ut til Sentral Print.",
			example = "2020-02-20"
	)
	private Date datoSendtPrint;
}
