package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UbehandletJournalpost {

	@Schema(
			description = "ID til journalpost i Joark",
			requiredMode = REQUIRED,
			example = "22345678"
	)
	private long journalpostId;

	@Schema(
			description = "JournalStatus i Joark",
			requiredMode = REQUIRED,
			example = "M"
	)
	private String journalStatus;

	@Schema(
			description = "Mottakskanal til journalpost i Joark",
			requiredMode = REQUIRED,
			example = "NAV_NO"
	)
	private String mottaksKanal;

	@Schema(
			description = "Bruker til journalpost i Joark"
	)
	private UbehandletBruker bruker;

	@Schema(
			description = "Temakode til journalpost i Joark",
			requiredMode = REQUIRED,
			example = "PEN"
	)
	private String tema;

	@Schema(
			description = "Behandlingstema journalpost i Joark",
			requiredMode = REQUIRED,
			example = "ab0001"
	)
	private String behandlingstema;

	@Schema(
			description = "Journalførende enhet for journalpost i Joark",
			requiredMode = REQUIRED,
			example = "0001"
	)
	private String journalforendeEnhet;

	@Schema(
			description = "Dato journalposten ble opprettet i Joark",
			requiredMode = REQUIRED,
			example = "2019-12-04T11:07:25.596+0000"
	)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private Date datoOpprettet;
}
