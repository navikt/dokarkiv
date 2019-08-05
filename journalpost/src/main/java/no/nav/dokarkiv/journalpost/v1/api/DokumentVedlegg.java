package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentVedlegg {

	@ApiModelProperty(
			value = "ID til journalpost kilden i Joark",
			example = "22345678",
			required = true)
	private Long kildeJournalpostId;

	@ApiModelProperty(
			value = "ID til dokumentet som skal legges til som vedlegg",
			example = "12345678",
			required = true)
	private String dokumentInfoId;

}
