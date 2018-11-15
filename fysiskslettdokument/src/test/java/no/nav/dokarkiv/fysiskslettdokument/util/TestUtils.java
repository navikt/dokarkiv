package no.nav.dokarkiv.fysiskslettdokument.util;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.fysiskslettdokument.rjoark102.FysiskSlettDokumentRequestTo;

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

	public static final Long JOURNALPOST_ID_TEST = 1L;
	public static final Long DOKUMENT_INFO_ID_TEST = 1L;
	public static final Long DOKUMENT_INFO_ID_TEST_VEDLEGG = 2L;
	public static final BegrensningTypeCode BEGRENSNINGTYPE_UTILGJENGELIGGJORT = BegrensningTypeCode.UTILGJENGELIGGJORT;
	public static final String HOVEDDOKUMENT = "hoveddokument";
	public static final String VEDLEGG = "vedlegg";

	private static Long journalpostId = 1L;
	private static Long dokumentInfoId = 1L;
	private static Long journalpostDokumentinfoRelasjonId = 1L;


	public static void resetIds() {
		journalpostId = 1L;
		dokumentInfoId = 1L;
		journalpostDokumentinfoRelasjonId = 1L;
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

	public static void opprettDuplikatRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		relasjon.getJournalpost().addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(relasjon.getDokumentInfo())
				.build());

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

	// SLETTELINJE -------------------------------------------------------------------


	public static FysiskSlettDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		return FysiskSlettDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.begrensningType(begrensningTypeCode)
				.build();
	}

	public static FysiskSlettDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId) {
		return createRequest(journalpostId, dokumentInfoId, BEGRENSNINGTYPE_UTILGJENGELIGGJORT);
	}

	public static FysiskSlettDokumentRequestTo createRequest() {
		return createRequest(JOURNALPOST_ID_TEST, DOKUMENT_INFO_ID_TEST, BEGRENSNINGTYPE_UTILGJENGELIGGJORT);
	}

	public static FysiskSlettDokumentRequestTo createRequest(JournalpostDokumentInfoRelasjon vedleggRelasjon) {
		return createRequest(
				vedleggRelasjon.getJournalpost().getJournalpostId(),
				vedleggRelasjon.getDokumentInfo().getDokumentInfoId(),
				BEGRENSNINGTYPE_UTILGJENGELIGGJORT);
	}

	public static JournalpostDokumentInfoRelasjon opprettOgReturnerVedleggRelasjonForEnhetstest(Boolean slettestatus) {
		Journalpost hoveddokument = opprettHoveddokumentForEnhetstest(false);
		hoveddokument.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
				.journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.dokumentInfo(
						getBaseDokumentInfoBuilder(slettestatus)
								.dokumentInfoId(dokumentInfoId++)
								.originalJournalpost(hoveddokument)
								.build())
				.build());

		return hoveddokument.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();
	}

	public static Journalpost oppretteDokumentMedEtVedleggForIT(Boolean sletteHoveddokument, Boolean sletteVedlegg) {
		Journalpost hoveddokument = opprettHoveddokumentForIT(sletteHoveddokument);
		hoveddokument.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.dokumentInfo(
						getBaseDokumentInfoBuilder(sletteVedlegg)
								.originalJournalpost(hoveddokument)
								.build())
				.build());

		return hoveddokument;
	}

	public static Journalpost oppretteDokumentOgKnyttVedleggForIt(Boolean slettHoveddokument, int antallVedlegg) {
		Journalpost hoveddokument = opprettHoveddokumentForIT(slettHoveddokument);

		while (antallVedlegg > 0) {
			hoveddokument.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
					.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
					.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
					.dokumentInfo(
							getBaseDokumentInfoBuilder(false)
									.originalJournalpost(hoveddokument)
									.build())
					.build());
			antallVedlegg--;
		}
		return hoveddokument;
	}

	public static JournalpostDokumentInfoRelasjon opprettOgReturnerHoveddokumentRelasjonForEnhetstest(Boolean slettestatus) {
		return opprettHoveddokumentForEnhetstest(slettestatus).findHoveddokumentDokumentInfoRelasjon();
	}

	public static Journalpost opprettHoveddokumentForEnhetstest(Boolean sletteStatus) {
		return getBaseJournalpostBuilder()
				.journalpostId(journalpostId++)
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getBaseDokumentInfoBuilder(sletteStatus)
										.dokumentInfoId(dokumentInfoId++)
										.build())
								.build())
				.build();
	}

	public static Journalpost opprettHoveddokumentForIT(Boolean sletteStatus) {
		return getBaseJournalpostBuilder()
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getBaseDokumentInfoBuilder(sletteStatus).build())
								.build())
				.build();
	}

	public static void knyttDokumentInfoSomVedleggTilJournalpostForIT(DokumentInfo dokInfoVedlegg, Journalpost jpHovedokument) {
		jpHovedokument.addJournalpostDokumentInfoRelasjon(
				getBaseJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.dokumentInfo(dokInfoVedlegg)
						.build());
	}


	private static DokumentInfoBuilder getBaseDokumentInfoBuilder(Boolean sletteStatus) {
		return DokumentInfoBuilder.getDokumentInfoBuilder()
				.slettet(sletteStatus)
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
