package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UbehandletJournalpost {

	@NotNull(message = "JournalpostId mangler")
	@Schema(
			description = "ID til journalpost i Joark",
			required = true,
			example = "22345678"
	)
	private long journalpostId;

	@NotNull(message = "JournalStatusCode mangler")
	@Schema(
			description = "JournalStatus i Joark",
			required = true,
			example = "M"
	)
	private String journalStatus;

	@Schema(
			description = "Mottakskanal til journalpost i Joark",
			required = true,
			example = "NAV_NO"
	)
	private String mottaksKanal;

	@Schema(
			description = "Bruker til journalpost i Joark"
	)
	private UbehandletBruker bruker;

	@Schema(
			description = "Temakode til journalpost i Joark",
			required = true,
			example = "PEN"
	)
	private String tema;

	@Schema(
			description = "Behandlingstema journalpost i Joark",
			required = true,
			example = "ab0001"
	)
	private String behandlingstema;

	@Schema(
			description = "Journalførende enhet for journalpost i Joark",
			required = true,
			example = "0001"
	)
	private String journalforendeEnhet;

	@NotNull(message = "datoOpprettet mangler for journalpost")
	@Schema(
			description = "Dato journalposten ble opprettet i Joark",
			required = true,
			example = "2019-12-04T11:07:25.596+0000"
	)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private Date datoOpprettet;
}
