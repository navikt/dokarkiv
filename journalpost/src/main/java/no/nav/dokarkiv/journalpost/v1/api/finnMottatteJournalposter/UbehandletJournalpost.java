package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
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
	@ApiModelProperty(
			value = "ID til journalpost i Joark",
			required = true,
			example = "22345678"
	)
	private long journalpostId;

	@NotNull(message = "journalStatusCode mangler")
	@ApiModelProperty(
			value = "journalStatus i Joark",
			required = true,
			example = "M"
	)
	private String journalStatus;

	@ApiModelProperty(
			value = "Mottakskanal til journalpost i Joark",
			required = true,
			example = "NAV_NO"
	)
	private String mottaksKanal;

	@ApiModelProperty(
			value = "Bruker til journalpost i Joark"
	)
	private UbehandletBruker bruker;

	@ApiModelProperty(
			value = "Temakode til journalpost i Joark",
			required = true,
			example = "PEN"
	)
	private String tema;

	@ApiModelProperty(
			value = "Behandlingstema journalpost i Joark",
			required = true,
			example = "ab0001"
	)
	private String behandlingstema;

	@ApiModelProperty(
			value = "journalførende enhet for journalpost i Joark",
			required = true,
			example = "0001"
	)
	private String journalforendeEnhet;

	@NotNull(message = "datoOpprettet mangler for journalpost")
	@ApiModelProperty(
			value = "Dato journalposten ble opprettet i Joark",
			required = true,
			example = "2019-12-04T11:07:25.596+0000"
	)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private Date datoOpprettet;
}
