package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;

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
	private JournalStatusCode journalStatus;

	@NotNull(message = "Mottakskanal til journalpost")
	@ApiModelProperty(
			value = "Mottakskanal til journalpost i Joark",
			required = true,
			example = "NAV_NO"
	)
	private MottaksKanalCode mottaksKanal;

	@NotNull(message = "Bruker til journalpost mangler")
	@ApiModelProperty(
			value = "Bruker til journalpost i Joark",
			required = true,
			example = "foo"
	)
	private UbehandletBruker bruker;

	@NotNull(message = "Temakode til journalpost mangler")
	@ApiModelProperty(
			value = "Temakode til journalpost i Joark",
			required = true,
			example = "PEN"
	)
	private FagomradeCode tema;

	@NotNull(message = "Behandlingstema til journalpost mangler")
	@ApiModelProperty(
			value = "Behandlingstema journalpost i Joark",
			required = true,
			example = "ab0001"
	)
	private Behandlingstema behandlingstema;

	@NotNull(message = "Journalførende enhet for journalpost mangler")
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
			example = "Fri Dec 06 15:55:00 CET 2019"
	)
	private Date datoOpprettet;
}
