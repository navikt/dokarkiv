package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
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

	@ApiModelProperty(
			value = "NAV-enheten som personen som utfører journalføring jobber for. Ved automatisk journalføring uten mennesker involvert, skal enhet settes til \"9999\".",
			required = true,
			example = "9999"
	)
	private String journalfoerendeEnhet;

	@Deprecated
	@ApiModelProperty(
			value = "Navn på saksbehandler eller system som journalførte.",
			required = false,
			example = "srvbruker"
	)
	private String journalfortAvNavn;

	@Deprecated
	@ApiModelProperty(
			value = "Navn på saksbehandler eller system som opprettet journalposten.",
			required = false,
			example = "srvbruker"
	)
	private String opprettetAvNavn;

	@Deprecated
	@ApiModelProperty(
			value = "Dato for ferdigstilling av dokumentet i Infotrygd.",
			required = false,
			example = "2020-02-20"
	)
	private Date datoJournal;

	@Deprecated
	@ApiModelProperty(
			value = "Datoen journalposten ble plukket ut til Sentral Print.",
			required = false,
			example = "2020-02-20"
	)
	private Date datoSendtPrint;
}
