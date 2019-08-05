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
			required = true)
	private String journalpostId;

	@ApiModelProperty(
			value = "Journalstatus for journalpost.\n" +
					"* MIDLERTIDIG - hvis journalpost er opprettet\n" +
					"* ENDELIG - hvis journalpost er opprett og endelig journalført",
			required = true,
			example = "MIDLERTIDIG")
	private String journalstatus;

	@ApiModelProperty(
			value = "Melding",
			required = false)
	private String melding;

	@ApiModelProperty(
			value = "True eller False for om journalpost ble ferdigstilt",
			example = "true",
			required = false)
	private Boolean journalpostferdigstilt;
}
