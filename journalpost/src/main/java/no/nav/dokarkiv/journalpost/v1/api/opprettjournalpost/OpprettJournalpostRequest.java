package no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpprettJournalpostRequest {

	@NotNull(message = "JournalpostType kan ikke være null")
	@ApiModelProperty(
			required = true,
			position = 2,
			example = "INNGAAENDE"
	)
	private JournalpostType journalpostType;

	@ApiModelProperty(
			value = "Avsender av forsendelsen",
			position = 7
	)
	private AvsenderMottaker avsenderMottaker;

	@ApiModelProperty(
			position = 8
	)
	private Bruker bruker;

	@ApiModelProperty(
			value = "Temaet som forsendelsen tilhører, for eksempel \"FOR\" (foreldrepenger).",
			position = 2,
			example = "DAG"

	)
	private String tema;

	@ApiModelProperty(
			value = "Behandlingstema for forsendelsen, for eksempel ab0001 (Ordinære dagpenger).",
			position = 3,
			example = "ab0001"
	)
	private String behandlingstema;

	@ApiModelProperty(
			value = "Tittel som beskriver forsendelsen samlet, feks \"Ettersendelse til søknad om foreldrepenger\".",
			position = 1,
			example = "Søknad om dagpenger ved permittering"
	)
	private String tittel;

	@ApiModelProperty(
			value = "Kanalen som ble brukt ved innsending eller distribusjon. F.eks. NAV_NO, ALTINN eller EESSI.",
			position = 5,
			example = "NAV_NO"
	)
	private String kanal;

	@ApiModelProperty(
			value = "NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen. " +
					"Ved automatisk journalføring uten mennesker involvert skal enhet settes til \"9999\".\n" +
					"Konsument må sette journalfoerendeEnhet dersom tjenesten skal ferdigstille journalføringen.",
			position = 6,
			example = "0701"
	)
	private String journalfoerendeEnhet;

	@ApiModelProperty(
			value = "Unik id for forsendelsen som kan brukes til sporing gjennom verdikjeden.\n" +
					"Eksempler på eksternReferanseId kan være sykmeldingsId for sykmeldinger, Altinn ArchiveReference for Altinn-skjema eller SEDid for SED.",
			hidden = true
	)
	private String eksternReferanseId;

	@ApiModelProperty(
			value = "Dato forsendelsen ble mottatt fra avsender. Dersom datoMottatt er tom, settes verdien til dagens dato.\n" +
					" Feltet kan kun settes for inngående journalposter.",
			dataType = "Date",
			example = "2019-11-29",
			hidden = true
	)
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date datoMottatt;

	@Builder.Default
	@ApiModelProperty(
			hidden = true
	)
	private List<Tilleggsopplysning> tilleggsopplysninger = new ArrayList<>();

	@ApiModelProperty(
			value = "Saken som journalposten hører til",
			position = 9
	)
	private Sak sak;

	@Builder.Default
	@NotNull(message = "dokumenter kan ikke være null")
	@ApiModelProperty(
			value = "Første dokument blir tilknyttet som hoveddokument på journalposten. Øvrige dokumenter tilknyttes som vedlegg. Rekkefølgen på vedlegg beholdes ikke ved uthenting av journalpost.",
			position = 10,
			required = true
	)
	private List<Dokument> dokumenter = new ArrayList<>();

}
