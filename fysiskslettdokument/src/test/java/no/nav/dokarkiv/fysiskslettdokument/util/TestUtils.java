package no.nav.dokarkiv.fysiskslettdokument.util;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestUtils {

	private static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Endret av navn";
	private static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	private static final String HOVEDDOKUMENT_TITTEL = "FysiskSlettDokument_Hoveddokument";
	private static final String VEDLEGG_TITTEL = "FysiskSlettDokument_Vedlegg";
	private static final String BREVGRUPPE = "Brevgruppe";
	private static final String BREVKODE = "Brevkode";
	private static final String FILNAVN = "filNavn";

	public static final Long JOURNALPOST_ID = 42L;
	public static final Long DOKUMENT_INFO_ID = 91L;
	public static final String HJEMMEL = "hjemmel fra XYZ";


	public static void setLogiskSlettetByDokumentInfo(DokumentInfo dokumentInfo) {
		dokumentInfo.setTittel(dokumentInfo.getTittel() + " - slettet");
		dokumentInfo.setSlettet(true);
	}

	public static List<Journalpost> opprettHoveddokumentOgVedlegg(int antallVedlegg) {
		List<Journalpost> journalpostList = new ArrayList<Journalpost>();

		Journalpost hoveddokument = opprettHoveddokument();
		journalpostList.add(hoveddokument);

		while (antallVedlegg > 0) {
			journalpostList.add(opprettKnyttetVedlegg(hoveddokument));
			antallVedlegg--;
		}
		return journalpostList;
	}

	public static Journalpost opprettHoveddokument() {
		return getBaseJournalpostBuilder()
				.dokumentInfoRelasjoner(getJpDokInfoRelForHoveddokument()).build();
	}

	private static Journalpost opprettKnyttetVedlegg(Journalpost jpHoveddokument) {
		return getBaseJournalpostBuilder()
				.dokumentInfoRelasjoner(getJpDokInfoRelForVedlegg(jpHoveddokument)).build();
	}


	private static JournalpostBuilder getBaseJournalpostBuilder() {
		return JournalpostBuilder.getJournalpostBuilder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.dokumentDato(new Date())
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalStatus(JournalStatusCode.FS)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.saksrelasjon(
						SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.brukere(
						BrukerTestDataProvider.createBruker().build())
				.mottakskanal(MottaksKanalCode.NAV_NO);
	}

	private static JournalpostDokumentInfoRelasjon getJpDokInfoRelForHoveddokument() {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(createDokumentInfoBuilder().build())
				.build();
	}

	private static JournalpostDokumentInfoRelasjon getJpDokInfoRelForVedlegg(Journalpost jpHoveddokument) {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfoVedleggBuilder(jpHoveddokument).build())
				.build();
	}

	public static DokumentInfoBuilder createDokumentInfoBuilder() {
		return getDokumentInfoBuilder()
				.slettet(false)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(HOVEDDOKUMENT_TITTEL)
				.endretAvNavn(ENDRET_AV_NAVN)
				.brevgruppe(BREVGRUPPE)
				.brevkode(BREVKODE)
				.filDetaljerList(createFildetaljer())
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN);
	}

	public static DokumentInfoBuilder createDokumentInfoVedleggBuilder(Journalpost originalJournalpost) {
		return getDokumentInfoBuilder()
				.slettet(false)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(VEDLEGG_TITTEL)
				//TODO: I databasen T_DOKUMENT_INFO er setOriginialJournalpost lagret som ID og ikke Journalpost
				.originalJournalpost(originalJournalpost)
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

	public static FysiskSlettDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId, String hjemmel) {
		return FysiskSlettDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.hjemmel(hjemmel)
				.build();
	}

	public static FysiskSlettDokumentRequestTo createRequest() {
		return createRequest(JOURNALPOST_ID, DOKUMENT_INFO_ID, HJEMMEL);
	}

	public static Journalpost opprettHoveddokumentForEnhetstest(Boolean sletteStatus) {
		Journalpost jp = getBaseJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(createDokumentInfoBuilder()
										.dokumentInfoId(DOKUMENT_INFO_ID)
										.build())
								.build())
				.build();
		if (sletteStatus) {
			setLogiskSlettetByDokumentInfo(jp.findDokumentInfoById(DOKUMENT_INFO_ID));
		}
		return jp;
	}

	public static Journalpost opprettVedleggForEnhetsTest(Boolean sletteStatus) {
		Journalpost jp = getBaseJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(createDokumentInfoVedleggBuilder(null)
										.dokumentInfoId(DOKUMENT_INFO_ID)
										.build())
								.build())
				.build();
		if (sletteStatus) {
			setLogiskSlettetByDokumentInfo(jp.findDokumentInfoById(DOKUMENT_INFO_ID));
		}
		return jp;
	}

	public static JournalpostDokumentInfoRelasjon getJpDokInfoRelasjonFromJp(Journalpost journalpost) {
		return journalpost.findDokumentInfoById(DOKUMENT_INFO_ID)
				.findJournalpostRelasjonByJournalpostId(journalpost.getJournalpostId());
	}
}
