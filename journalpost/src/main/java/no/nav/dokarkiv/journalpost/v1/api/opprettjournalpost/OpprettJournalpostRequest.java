package no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.DateStringsToLocalDateTimeDeserializer;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpprettJournalpostRequest {

	@Schema(
			requiredMode = REQUIRED,
			description = """
					INNGAAENDE brukes for dokumentasjon som NAV har mottatt fra en ekstern part. Dette kan være søknader, ettersendelser av dokumentasjon til sak eller meldinger fra arbeidsgivere.
					
					UTGAAENDE brukes for dokumentasjon som NAV har produsert og sendt ut til en ekstern part. Dette kan for eksempel være informasjons- eller vedtaksbrev til privatpersoner eller organisasjoner.
					
					NOTAT brukes for dokumentasjon som NAV har produsert selv og uten mål om å distribuere dette ut av NAV. Eksempler på dette er forvaltningsnotater og referater fra telefonsamtaler med brukere.
					""",
			example = "INNGAAENDE"
	)
	private JournalpostType journalposttype;

	@Schema(
			description = """
					* Ved journalposttype INNGÅENDE skal avsender av dokumentene oppgis.
					* Ved journalposttype UTGÅENDE skal mottaker av dokumentene oppgis.
					* avsenderMottaker skal ikke settes for journalposttype NOTAT.
					"""
	)
	private AvsenderMottaker avsenderMottaker;

	@Schema(
			description = "Brukeren som forsendelsen gjelder"
	)
	private Bruker bruker;

	@Schema(
			description = """
					Temaet som forsendelsen tilhører, for eksempel 'DAG' (Dagpenger).
					Tema er påkrevd dersom Sak oppgis.
					""",
			example = "DAG"
	)
	private String tema;

	@Schema(
			description = """
					Behandlingstema for forsendelsen, for eksempel ab0001 (Ordinære dagpenger).
					Lovlige verdier finnes i i Felles Kodeverksløsning, men valideres ikke.
					""",
			example = "ab0001"
	)
	private String behandlingstema;

	@Schema(
			description = """
					Tittel som beskriver forsendelsen samlet.
					""",
			example = "Søknad om dagpenger ved permittering"
	)
	private String tittel;

	@Schema(
			description = """
					Kanalen som ble brukt ved innsending eller distribusjon. F.eks. NAV_NO, ALTINN eller EESSI. Kanal skal ikke settes for notater.
					""",
			example = "NAV_NO"
	)
	private String kanal;

	@Schema(
			description = """
					NAV-enheten som har journalført forsendelsen.
					
					Dersom forsoekFerdigstill=true skal enhet alltid settes. Dersom  det ikke er noen Nav-enhet involvert (f.eks. ved automatisk brevutsending), skal enhet være '9999'.
					
					Dersom foersoekFerdigstill=false bør journalførendeEnhet kun settes dersom oppgavene skal rutes på en annen måte enn Norg-reglene tilsier. Hvis enhet er bank, havner oppgavene på enheten som ligger i Norg-regelsettet.
					""",
			example = "0701"
	)
	private String journalfoerendeEnhet;

	@Schema(
			requiredMode = REQUIRED,
			description = """
					Unik id for forsendelsen som kan brukes til sporing gjennom verdikjeden. Eksempler på eksternReferanseId kan være en GUID, sykmeldingsId for sykmeldinger, Altinn ArchiveReference for Altinn-skjema eller SEDid for SED.
					
					NB: Det er duplikatkontroll på eksternReferanseId. Dersom man sender inn en eksternReferanseId som allerede finnes i arkivet, vil tjenesten kaste feil (409 Conflict).
					""",
			example = "a0f480a3-8ab2-4c56-8c93-e53bb35bec2b"
	)
	private String eksternReferanseId;

	@Schema(
			description = """
					Brukes for å datere innholdet i hoveddokumentet.
					
					Skal kun brukes dersom innholdet i dokumentet har en annen datering enn tidspunktet for opprettelse av journalposten.
					Sett lokaltid Europe/Oslo på format "yyyy-MM-dd'T'HH:mm:ss.SSS"
					""",
			example = "2024-04-14T10:58:53.470"
	)
	private LocalDateTime datoDokument;

	@Schema(
			description = """
					Dato forsendelsen ble mottatt fra avsender. Dersom datoMottatt er tom, settes verdien til dagens dato.
					
					Feltet kan kun settes for inngående journalposter.
					Sett lokaltid Europe/Oslo på format `"yyyy-MM-dd'T'HH:mm:ss.SSS"`. `java.time.LocalDateTime` kan brukes som felt-type og serialiseringsbibliotek som jackson vil vanligvis gi denne representasjonen.
					Alternativt UTC på format `"yyyy-MM-dd'T'HH:mm:ss.SSSXXX"`. `java.time.OffsetDateTime` kan brukes som felt-type og serialiseringsbibliotek som jackson vil vanligvis gi denne representasjonen.
					""",
			example = "2024-04-14T10:58:53.470"
	)
	@JsonDeserialize(using = DateStringsToLocalDateTimeDeserializer.class)
	private LocalDateTime datoMottatt;

	@Builder.Default
	@ArraySchema(arraySchema = @Schema(
			description = """
					Kan brukes av fagsystemene til å lagre egne fagspesifikke attributter per journalpost. Nøkkel-verdi-settet er skjemaløst og valideres ikke. Nøkkelen bør prefixes med fagsystemets navn for å unngå "kollisjon" mellom fagsystemer.
					
					Et eksempel er nøkkel eessi_bucid og verdi 1234
					""")
	)
	private List<Tilleggsopplysning> tilleggsopplysninger = new ArrayList<>();

	@Schema(
			description = "Saken som journalposten hører til"
	)
	private Sak sak;

	@Builder.Default
	@ArraySchema(arraySchema = @Schema(
			description = """
					Alle dokumentene som skal arkiveres.
					
					Det første dokument i meldingen blir tilknyttet som hoveddokument på journalposten. Øvrige dokumenter tilknyttes som vedlegg. Rekkefølgen på vedlegg kan settes med felt "rekkefoelge".
					""",
			requiredMode = REQUIRED)
	)
	private List<Dokument> dokumenter = new ArrayList<>();

	@Schema(
			description = """
					Gjør at journalposten med dokumenter vises til pålogget bruker på nav.no til tross for at standardregelsettet sier at journalposten og/eller dokumentene skal skjules.
					Dersom flagget ikke settes, er det [standard-regelsettet](https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn) som styrer innsyn.
					* VISES_MASKINELT_GODKJENT brukes når en maskinell prosess har besluttet at journalposten og underliggende dokumenter kan vises til bruker på nav.no.
					* VISES_MANUELT_GODKJENT brukes når en NAV-ansatt har sett over og godkjent at journalposten og underliggende dokumenter kan vises til bruker på nav.no.
					""",
			example = "VISES_MANUELT_GODKJENT",
			nullable = true
	)
	private String overstyrInnsynsregler;

	@JsonIgnore
	public boolean isInngaaende() {
		return journalposttype == JournalpostType.INNGAAENDE;
	}
}
