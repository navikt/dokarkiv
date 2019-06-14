package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentVedlegg {

	@NotNull(message = "ID til journalpost kilden mangler")
	@ApiModelProperty(
			value = "ID til journalpost kilden i Joark",
			required = true)
	private Long kildeJournalpostId;

	@NotNull(message = "DokumentinfoId mangler")
	@ApiModelProperty(
			value = "ID til dokumentet som skal legges til som vedlegg",
			required = true)
	private String dokumentInfoId;

}
