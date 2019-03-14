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
			value = "Avsender av forsendelsen",
			required = false)
	private AvsenderMottaker avsenderMottaker;

	@ApiModelProperty(
			value = "",
			required = false)
	private Bruker bruker;

	@ApiModelProperty(
			value = "Fagområdet som forsendelsen tilhører, for eksempel \"FOR\" for Foreldrepenger",
			example = "FOR",
			required = true)
	private String tema;

	@ApiModelProperty(
			value = "",
			required = false)
	private String behandlingstema;

	@ApiModelProperty(
			value = "Tittel som beskriver forsendelsen samlet, feks \"Ettersendelse til søknad om foreldrepenger\".",
			example = "Ettersendelse til søknad om foreldrepenger",
			required = true)
	private String tittel;

	@ApiModelProperty(
			value = "Kanalen som ble brukt ved innsending eller distribusjon. F.eks. NAV_NO, ALTINN eller EESSI.",
			example = "NAV_NO",
			required = false)
	private String kanal;

	@ApiModelProperty(
			value = "NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen.\n" +
					"Ved automatisk journalføring uten mennesker involvert skal enhet settes til \"9999\".",
			example = "9999",
			required = false)
	private String journalfoerendeEnhet;

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
	private Sak sak;

	@Builder.Default
	@ApiModelProperty(
			value = "Første dokument blir tilknyttet som hoveddokument på journalposten. Øvrige dokumenter tilknyttes som vedlegg. Rekkefølgen på vedlegg beholdes ikke ved uthenting av journalpost.",
			required = true)
	private List<Dokument> dokumenter = new ArrayList<>();

}
