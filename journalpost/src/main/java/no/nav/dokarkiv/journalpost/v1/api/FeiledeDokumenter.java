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
public class FeiledeDokumenter {

	@NotNull(message = "ID til journalpost kilden mangler")
	@ApiModelProperty(
			value = "ID til journalpost kilden i Joark",
			example = "22345678",
			required = true)
	private String kildeJournalpostId;

	@ApiModelProperty(
			value = "ID til dokumentet som skal legges til som vedlegg",
			example = "12345678",
			required = true)
	private String dokumentInfoId;

	@ApiModelProperty(
			value = "Årsak til at dokumentet ikke lot seg knytte til journalpostId",
			example = "UGYLDIG_STATUS",
			required = true)
	private ArsakKode arsakKode;



}
