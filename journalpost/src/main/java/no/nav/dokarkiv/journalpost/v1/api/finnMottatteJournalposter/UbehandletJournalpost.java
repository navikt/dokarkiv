package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = NON_NULL)
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
	private JournalStatusCode journalStatus;

	@Schema(
			description = "Mottakskanal til journalpost i Joark",
			requiredMode = REQUIRED,
			example = "NAV_NO"
	)
	private MottaksKanalCode mottaksKanal;

	@Schema(
			description = "Bruker til journalpost i Joark. Returneres bare til interne tjenester i dokumentløsninger."
	)
	@Hidden
	private UbehandletBruker bruker;

	@Schema(
			description = "Temakode til journalpost i Joark",
			requiredMode = REQUIRED,
			example = "PEN"
	)
	private FagomradeCode tema;

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
