package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OppdaterJournalpostResponse {
	@NotNull(message = "OppdaterJournalpostResponse mangler journalpostId")
	@ApiModelProperty(
			value = "JournalpostId som har blitt oppdatert (og forsøkt endelig journalført)",
			required = true)
	private String journalpostId;
}
