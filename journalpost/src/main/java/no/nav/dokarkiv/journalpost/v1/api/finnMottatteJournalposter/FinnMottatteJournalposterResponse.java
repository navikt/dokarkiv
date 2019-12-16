package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@AllArgsConstructor
public class FinnMottatteJournalposterResponse {
	@NotNull(message = "FinnMottatteJournalposterResponse mangler Journalposter")
	@ApiModelProperty(
			dataType="List",
			value = "journalposter",
			required = true
	)
	private List<UbehandletJournalpost> journalposter;
}
