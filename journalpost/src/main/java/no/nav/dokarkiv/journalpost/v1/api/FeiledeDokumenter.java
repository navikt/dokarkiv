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
			required = true,
			example = "22345678"
	)
	private String kildeJournalpostId;

	@ApiModelProperty(
			value = "ID til dokumentet som skal legges til som vedlegg",
			required = true,
			example = "12345678"
	)
	private String dokumentInfoId;

	@ApiModelProperty(
			value = "Årsak til at dokumentet ikke lot seg knytte til journalpostId",
			required = true,
			example = "UGYLDIG_STATUS"
	)
	private ArsakKode arsakKode;



}
