package no.nav.dokarkiv.util;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import no.nav.dokarkiv.dto.SkjermArkivenhetRequest;

import java.util.Date;

public class TestUtil {
	private static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Endret av navn";
	private static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	private static final String BREVGRUPPE = "Brevgruppe";
	private static final String BREVKODE = "Brevkode";
	private static final String FILNAVN = "filNavn";
	private static final String TITTEL = "Tittel";
	public static String KASSERT_AV_NAVN = "Kåre Kassasjon";
	public static Long JOURNALPOST_ID = 2000000L;
	public static Long JPDOKINFORELAJSON_ID = 2000000L;
	public static Long DOKUMENTINFO_ID = 2000000L;
	public static String FIL_UUID_ARKIV= "ARKIV_F";
	public static String FIL_UUID_SLADDET= "SLADDET_F";


	public static SkjermArkivenhetRequest createSkjermarkivenhetRequest(SkjermingTypeCode skjermingType, ArkivenhetCode arkivenhet, Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {
		return SkjermArkivenhetRequest.builder()
				.skjerming(skjermingType)
				.arkivenhet(arkivenhet)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.variant(variantFormat)
				.build();
	}


	public static Journalpost opprettHoveddokumentForIT() {
		return getBaseJournalpostBuilder()
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getBaseDokumentInfoBuilder().build())
								.build())
				.build();
	}

	public static Journalpost opprettHoveddokumentForEnhetstest() {
		return getBaseJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID++)
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.journalpostDokumentInfoRelasjonId(JPDOKINFORELAJSON_ID++)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getBaseDokumentInfoBuilder()
										.dokumentInfoId(DOKUMENTINFO_ID)
										.build())
								.build())
				.build();
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

	public static void knyttDokumentInfoSomVedleggTilJournalpost(DokumentInfo dokInfoVedlegg, Journalpost jpHovedokument) {
		jpHovedokument.addJournalpostDokumentInfoRelasjon(
				getBaseJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.dokumentInfo(dokInfoVedlegg)
						.build());
	}

	public static KasserDokumentRequest createKasserDokumentRequest(Long dokumentInfoId) {
		return KasserDokumentRequest.builder().dokumentInfoId(dokumentInfoId).kassertAvNavn(KASSERT_AV_NAVN).build();
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
				.filDetaljerList(createFildetaljer(VariantFormatCode.ARKIV, FIL_UUID_ARKIV), createFildetaljer(VariantFormatCode.SLADDET, FIL_UUID_SLADDET))
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN);
	}

	private static FilDetaljer createFildetaljer(VariantFormatCode variantFormatCode, String filUuid) {
		return FilDetaljerBuilder.getFilDetaljerBuilder()
				.filUuid(filUuid)
				.filnavn(FILNAVN)
				.filtype(FilTypeCode.PDF)
				.variantFormat(variantFormatCode)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fileContent("ARKIV variant".getBytes())
				.build();
	}
}

