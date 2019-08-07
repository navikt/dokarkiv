package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpprettJournalpostResponse {

	@ApiModelProperty(
			value = "JournalpostId som har blit opprettet",
			required = true,
			example = "12345678"
	)
	private String journalpostId;

	@ApiModelProperty(
			value = "Journalstatus for journalpost.\n" +
					"* MIDLERTIDIG - hvis journalpost er opprettet\n" +
					"* ENDELIG - hvis journalpost er opprett og endelig journalført\n\n " +
					"Feltet er deprekert og vil bli fjernet i fremtiden. Bruk journalpostferdigstilt i stedet.",
			required = true,
			example = "ENDELIG")
	@Deprecated
	/**
	 * @deprecated
	 */
	private String journalstatus;

	@ApiModelProperty(
			value = "Melding",
			example = "null"
	)
	private String melding;

	@ApiModelProperty(
			value = "True eller False for om journalpost ble ferdigstilt",
			example = "true"
	)
	private Boolean journalpostferdigstilt;
}
