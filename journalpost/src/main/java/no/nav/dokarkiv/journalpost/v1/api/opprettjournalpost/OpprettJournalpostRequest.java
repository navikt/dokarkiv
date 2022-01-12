package no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpprettJournalpostRequest {

	@NotNull(message = "Journalposttype kan ikke være null")
	@Schema(
			required = true,
			example = "INNGAAENDE"
	)
	private JournalpostType journalposttype;

	@Schema(
			description = "Avsender av forsendelsen"
	)
	private AvsenderMottaker avsenderMottaker;

	@Schema
	private Bruker bruker;

	@Schema(
			description = """
					Temaet som forsendelsen tilhører, for eksempel “DAG” (Dagpenger).
					Tema er påkrevd dersom Sak oppgis.
					""",
			example = "DAG"
	)
	private String tema;

	@Schema(
			description = """
					Behandlingstema for forsendelsen, for eksempel ab0001 (Ordinære dagpenger).
					Lovlige verdier finnes i felles kodeverksløsning.
					""",
			example = "ab0001"
	)
	private String behandlingstema;

	@Schema(
			description = """
					Tittel som beskriver forsendelsen samlet, f.eks. "Søknad om dagpenger ved permittering".
					""",
			example = "Søknad om dagpenger ved permittering"
	)
	private String tittel;

	@Schema(
			description = "Kanalen som ble brukt ved innsending eller distribusjon. F.eks. NAV_NO, ALTINN eller EESSI.",
			example = "NAV_NO"
	)
	private String kanal;

	@Schema(
			description = """
					NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen.
					Ved automatisk journalføring uten mennesker involvert skal enhet settes til "9999".
					Konsument må sette journalfoerendeEnhet dersom tjenesten skal ferdigstille journalføringen.
					""",
			example = "0701"
	)
	private String journalfoerendeEnhet;

	@Schema(
			description = """
					Unik id for forsendelsen som kan brukes til sporing gjennom verdikjeden.
					Eksempler på eksternReferanseId kan være en GUID, sykmeldingsId for sykmeldinger, Altinn ArchiveReference for Altinn-skjema eller SEDid for SED.
					NB: Det er duplikatkontroll på eksternReferanseId. Dersom man sender inn en eksternReferanseId som allerede finnes i arkivet, vil tjenesten kaste feil (409 Conflict).
					""",
			example = "a0f480a3-8ab2-4c56-8c93-e53bb35bec2b"
	)
	private String eksternReferanseId;

	// TODO: Sjekk at type er dato etter fjerning av dataType = "Date"
	@Schema(
			description = """
					Dato forsendelsen ble mottatt fra avsender. Dersom datoMottatt er tom, settes verdien til dagens dato.
					Feltet kan kun settes for inngående journalposter.
					""",
			example = "2020-01-01"
	)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date datoMottatt;

	@Builder.Default
	@ArraySchema(arraySchema = @Schema(
			description = """
					Fagsystemene som arkiverer kan legge til egne fagspesifikke attributter per journalpost. Disse er representert
					som et skjemaløst nøkkel-verdi-sett og valideres ikke ved arkivering. Et eksempel på et slikt sett kan være
					nøkkel: bucid og verdi: 12345.
					"""
		)
	)
	private List<Tilleggsopplysning> tilleggsopplysninger = new ArrayList<>();

	@Schema(
			description = "Saken som journalposten hører til"
	)
	private Sak sak;

	@Builder.Default
	@NotNull(message = "dokumenter kan ikke være null")
	@ArraySchema(arraySchema=@Schema(
			description = "Første dokument blir tilknyttet som hoveddokument på journalposten. Øvrige dokumenter tilknyttes som vedlegg. Rekkefølgen på vedlegg beholdes ikke ved uthenting av journalpost.",
			required = true
		)
	)
	private List<Dokument> dokumenter = new ArrayList<>();

	@JsonIgnore
	public boolean isInngaaende() {
		return journalposttype == JournalpostType.INNGAAENDE;
	}
}
