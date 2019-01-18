package no.nav.dokarkiv.fysiskslettdokument.util;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

import java.util.Date;

public class TestUtils {

	public static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	public static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	public static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	public static final String ENDRET_AV_NAVN = "Endret av navn";
	public static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	public static final String TITTEL = "FysiskSlettDokument";
	public static final String BREVGRUPPE = "Brevgruppe";
	public static final String BREVKODE = "Brevkode";
	public static final String FILNAVN = "filNavn";


	public static final SkjermingTypeCode BEGRENSNINGTYPE_POL = SkjermingTypeCode.POL;

	public static Journalpost opprettHoveddokumentForIT() {
		return getBaseJournalpostBuilder()
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getBaseDokumentInfoBuilder().build())
								.build())
				.build();
	}

	public static void opprettDuplikatRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		relasjon.getJournalpost().addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(relasjon.getDokumentInfo())
				.build());

	}

	public static Journalpost opprettHoveddokumentMedEtKnyttetVedleggForIT() {
		Journalpost journalpost = opprettHoveddokumentForIT();
		journalpost.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.dokumentInfo(getBaseDokumentInfoBuilder()
						.originalJournalpost(journalpost)
						.build())
				.build());
		return journalpost;
	}

	public static Journalpost opprettHoveddokumentMedSammensattDokForIT() {
		Journalpost journalpost = opprettHoveddokumentForIT();
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK)
				.dokumentInfo(getBaseDokumentInfoBuilder().build())
				.build());
		return journalpost;
	}

	public static void knyttDokumentInfoSomVedleggTilJournalpostForIT(DokumentInfo dokInfoVedlegg, Journalpost jpHovedokument) {
		jpHovedokument.addJournalpostDokumentInfoRelasjon(
				getBaseJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.dokumentInfo(dokInfoVedlegg)
						.build());
//		dokInfoVedlegg.setOriginalJournalpost(jpHovedokument);
	}

	public static Begrensning utilgjengeliggjoerHoveddokument(Long journalpostId) {
		Begrensning begrensning = Begrensning.builder()
				.journalpostId(journalpostId)
				.begrensningType(SkjermingTypeCode.POL)
				.build();
		begrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return begrensning;
	}

	public static Begrensning utilgjengeliggjoerVedlegg(Long journalpostId, Long dokumentInfoId) {
		Begrensning begrensning = Begrensning.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.begrensningType(SkjermingTypeCode.POL)
				.build();
		begrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return begrensning;
	}


	private static JournalpostBuilder getBaseJournalpostBuilder() {
		return JournalpostBuilder.getJournalpostBuilder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.dokumentDato(new Date())
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalStatus(JournalStatusCode.FS)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.addOriginalJournalpost(true)
				.fagomrade(FagomradeCode.RPO)
				.saksrelasjon(
						SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.brukere(
						BrukerTestDataProvider.createBruker().build())
				.mottakskanal(MottaksKanalCode.NAV_NO);
	}

	private static JournalpostDokumentInfoRelasjonBuilder getBaseJournalpostDokumentInfoRelasjonBuilder() {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN);
	}

	private static DokumentInfoBuilder getBaseDokumentInfoBuilder() {
		return DokumentInfoBuilder.getDokumentInfoBuilder()
				.tittel(TITTEL)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.endretAvNavn(ENDRET_AV_NAVN)
				.brevgruppe(BREVGRUPPE)
				.brevkode(BREVKODE)
				.filDetaljerList(createFildetaljer())
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN);
	}

	private static FilDetaljer createFildetaljer() {
		return createFildetaljer(FilDetaljer.generateUuid());
	}

	private static FilDetaljer createFildetaljer(String filUuid) {
		return FilDetaljerBuilder.getFilDetaljerBuilder()
				.filUuid(filUuid)
				.filnavn(FILNAVN)
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ARKIV)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}
}
