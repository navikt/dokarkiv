package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@ApiModel
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpprettJournalpostRequest {

	@ApiModelProperty(
			value = "",
			required = true)
	private JournalpostType journalpostType;

	@ApiModelProperty(
			value = "",
			required = false)
	private AvsenderMottaker avsenderMottaker;

	@ApiModelProperty(
			value = "",
			required = false)
	private Bruker bruker;

	@ApiModelProperty(
			value = "",
			required = true)
	private String tema;

	@ApiModelProperty(
			value = "",
			required = false)
	private String behandlingstema;

	@ApiModelProperty(
			value = "",
			required = true)
	private String tittel;

	@ApiModelProperty(
			value = "",
			required = false)
	private String kanal;

	@ApiModelProperty(
			value = "",
			required = false)
	private String eksternReferanseId;

	@Builder.Default
	@ApiModelProperty(
			value = "",
			required = false)
	private List<Tilleggsopplysning> tilleggsopplysninger = new ArrayList<>();

	@ApiModelProperty(
			value = "",
			required = false)
	private Arkivsak arkivSak;

	@Builder.Default
	@ApiModelProperty(
			value = "Første dokument blir tilknyttet som hoveddokument på journalposten. Øvrige dokumenter tilknyttes som vedlegg. Rekkefølgen på vedlegg beholdes ikke ved uthenting av journalpost.",
			required = false)
	private List<Dokument> dokumenter = new ArrayList<>();

}
