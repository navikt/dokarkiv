package no.nav.dokarkiv.fysiskslettdokument.util;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
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
import no.nav.dokarkiv.logiskslettdokument.common.SlettemeldingsFunksjoner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestUtils {

	public static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	public static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	public static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	public static final String ENDRET_AV_NAVN = "Endret av navn";
	public static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	public static final String HOVEDDOKUMENT_TITTEL = "FysiskSlettDokument_Hoveddokument";
	public static final String VEDLEGG_TITTEL = "FysiskSlettDokument_Vedlegg";
	public static final String TITTEL = "FysiskSlettDokument";
	public static final String BREVGRUPPE = "Brevgruppe";
	public static final String BREVKODE = "Brevkode";
	public static final String FILNAVN = "filNavn";

	public static final Long JOURNALPOST_ID_TEST = 1L;
	public static final Long DOKUMENT_INFO_ID_TEST = 1L;
	public static final Long DOKUMENT_INFO_ID_TEST_VEDLEGG = 2L;
	public static final String HJEMMEL = "hjemmel fra XYZ";
	public static final String HJEMMEL_VEDLEGG = "fysiskSlettEtVedleggKnyttetEnJP";
	public static final String HJEMMEL_HOVEDDOKUMENT = "fysiskSlettEtHoveddokumentKnyttetEnJP";

	private static Long journalpostId = 1L;
	private static Long dokumentInfoId = 1L;
	private static Long journalpostDokumentinfoRelasjonId = 1L;


	public static void resetIds() {
		journalpostId = 1L;
		dokumentInfoId = 1L;
		journalpostDokumentinfoRelasjonId = 1L;
	}

	public static FysiskSlettDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId, String hjemmel) {
		return FysiskSlettDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.hjemmel(hjemmel)
				.build();
	}

	public static FysiskSlettDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId) {
		return createRequest(journalpostId, dokumentInfoId, HJEMMEL);
	}

	public static FysiskSlettDokumentRequestTo createRequest() {
		return createRequest(JOURNALPOST_ID_TEST, DOKUMENT_INFO_ID_TEST, HJEMMEL);
	}

	public static FysiskSlettDokumentRequestTo createRequest(JournalpostDokumentInfoRelasjon vedleggRelasjon) {
		return createRequest(
				vedleggRelasjon.getJournalpost().getJournalpostId(),
				vedleggRelasjon.getDokumentInfo().getDokumentInfoId(),
				HJEMMEL);
	}

	public static JournalpostDokumentInfoRelasjon opprettOgReturnerVedleggRelasjonForEnhetstest(Boolean slettestatus) {
		Journalpost hoveddokument = opprettHoveddokumentForEnhetstest(false);
		hoveddokument.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
				.journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.dokumentInfo(
						opprettDokumentInfoBuilder(slettestatus, TITTEL)
								.dokumentInfoId(dokumentInfoId++)
								.originalJournalpost(hoveddokument)
								.build())
				.build());

		return hoveddokument.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();
	}

	public static JournalpostDokumentInfoRelasjon opprettOgReturnerHoveddokumentMedKnyttetVedleggForIt() {
//		Journalpost hoveddokument = opprettHoveddokumentForIT(false);
//		hoveddokument.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
//				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
//				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
//				.dokumentInfo(
//						opprettDokumentInfoBuilder(slettestatus, TITTEL)
//								.originalJournalpost(hoveddokument)
//								.build())
//				.build());

		Journalpost hoveddokument = oppretteDokumentMedEtVedleggForIT(false, true);
		return hoveddokument.findHoveddokumentDokumentInfoRelasjon();
	}

	public static Journalpost oppretteDokumentMedEtVedleggForIT(Boolean sletteHoveddokument, Boolean sletteVedlegg) {
		Journalpost hoveddokument = opprettHoveddokumentForIT(sletteHoveddokument);
		hoveddokument.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.dokumentInfo(
						opprettDokumentInfoBuilder(sletteVedlegg, TITTEL)
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
							opprettDokumentInfoBuilder(false, TITTEL)
									.originalJournalpost(hoveddokument)
									.build())
					.build());
			antallVedlegg--;
		}
		return hoveddokument;
	}


	public static JournalpostDokumentInfoRelasjon opprettOgReturnerVedleggRelasjonForIT(Boolean slettestatus) {
//		Journalpost hoveddokument = opprettHoveddokumentForIT(false);
//		hoveddokument.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
//				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
//				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
//				.dokumentInfo(
//						opprettDokumentInfoBuilder(slettestatus, TITTEL)
//								.originalJournalpost(hoveddokument)
//								.build())
//				.build());

		Journalpost hoveddokument = oppretteDokumentMedEtVedleggForIT(false, true);
		return hoveddokument.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();
	}


	public static JournalpostDokumentInfoRelasjon opprettOgReturnerHoveddokumentRelasjonForEnhetstest(Boolean slettestatus) {
		return opprettHoveddokumentForEnhetstest(slettestatus).findHoveddokumentDokumentInfoRelasjon();
	}

	private static Journalpost opprettHoveddokumentForEnhetstest(Boolean sletteStatus) {
		return getBaseJournalpostBuilder()
				.journalpostId(journalpostId++)
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(opprettDokumentInfoBuilder(sletteStatus, TITTEL)
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
								.dokumentInfo(opprettDokumentInfoBuilder(sletteStatus, TITTEL).build())
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


	public static Journalpost opprettDokumentMedEtVedleggSomSkalSlettesForIT(Boolean sletteStatus) {
		return getBaseJournalpostBuilder()
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(opprettDokumentInfoBuilder(sletteStatus, TITTEL)
										.build())
								.build())
				.build();
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

	private static DokumentInfoBuilder opprettDokumentInfoBuilder(Boolean sletteStatus, String tittel) {
		//TODO: Etter bekreftet at vi ikke skal ha kvar - slettet i tittel så fjern denne
		tittel = setSletteTittelINyDokumentInfo(sletteStatus, tittel);

		return DokumentInfoBuilder.getDokumentInfoBuilder()
				.slettet(sletteStatus)
				.tittel(tittel)
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


	//Usikker om er i bruk ----------------------------------------------------------


	public static void setLogiskSlettetByDokumentInfo(DokumentInfo dokumentInfo) {
		dokumentInfo.setTittel(dokumentInfo.getTittel() + SlettemeldingsFunksjoner.getSlettemelding());
		dokumentInfo.setSlettet(true);
	}

	private static String setSletteTittelINyDokumentInfo(Boolean sletteStatus, String tittel) {
		if (sletteStatus) {
			tittel += SlettemeldingsFunksjoner.getSlettemelding();
		}
		return tittel;
	}


	public static void knyttJournalpostSomVedleggTilJournalpostForIT(Journalpost vedlegg, Journalpost hoveddokument) {
		hoveddokument.addJournalpostDokumentInfoRelasjon(
				getBaseJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.dokumentInfo(vedlegg.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
						.build());
	}

	public static void knyttJournalpostSomVedleggTilJournalpostForEnhetstest(Journalpost vedlegg, Journalpost hoveddokument) {
		hoveddokument.addJournalpostDokumentInfoRelasjon(
				getBaseJournalpostDokumentInfoRelasjonBuilder()
						.journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++)
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.dokumentInfo(vedlegg.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
						.build());
	}

	public static Journalpost opprettDokumentForIT(Boolean sletteStatus) {
		return getBaseJournalpostBuilder()
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(opprettDokumentInfoBuilder(sletteStatus, TITTEL)
										.build())
								.build())
				.build();
	}


	//SLETTELINJE


	public static List<JournalpostDokumentInfoRelasjon> opprettJournalpostDokumentInfoRelasjonerFraJournalposter(List<Journalpost> jpList) {
		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();

		for (Journalpost journalpost : jpList) {
			while (journalpost.getJournalpostDokumentInfoRelasjoner().iterator().hasNext()) {
				jpDokInfoRelasjoner.add(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next());
			}
		}

		return jpDokInfoRelasjoner;
	}

	//SLETT HVIS IKKE I BRUK
	public static List<Journalpost> opprettHoveddokumentOgKnyttVedleggForEnhetstest(int antallVedlegg) {
		List<Journalpost> journalpostList = new ArrayList<Journalpost>();

		while (antallVedlegg > 0) {
			Journalpost vedlegg = opprettHoveddokumentForEnhetstest(false);
			journalpostList.add(vedlegg);
			antallVedlegg--;
		}

		Journalpost hoveddokument = opprettHoveddokumentForEnhetstest(false);

//		journalpostList.forEach(vedlegg-> hoveddokument.addJournalpostDokumentInfoRelasjon(
//				getBaseJournalpostDokumentInfoRelasjonBuilder()
//						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
//						.dokumentInfo(vedlegg.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
//				.build()));

//		journalpostList.forEach(vedlegg -> knyttJournalpostSomVedleggTilJournalpostForEnhetstest(vedlegg, hoveddokument));

		journalpostList.add(hoveddokument);

		return journalpostList;
	}

	/**

	 public static Journalpost opprettHoveddokumentForEnhetsTest(Boolean sletteStatus) {
	 return getBaseJournalpostBuilder()
	 .journalpostId(journalpostId++)
	 .dokumentInfoRelasjoner(
	 getBaseJournalpostDokumentInfoRelasjonBuilder(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
	 .journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++)
	 .dokumentInfo(opprettDokumentInfoBuilder(sletteStatus, HOVEDDOKUMENT_TITTEL)
	 .dokumentInfoId(dokumentInfoId++)
	 .build())
	 .build())
	 .build();
	 }


	 public static Journalpost opprettKnyttetVedleggForEnhetsTest(Boolean sletteStatus, Journalpost originalJournalpost) {
	 Journalpost vedlegg = getBaseJournalpostBuilder()
	 .journalpostId(journalpostId++)
	 .dokumentInfoRelasjoner(
	 getBaseJournalpostDokumentInfoRelasjonBuilder(TilknyttetJournalpostSomCode.VEDLEGG)
	 .journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++)
	 .dokumentInfo(opprettDokumentInfoBuilder(sletteStatus, VEDLEGG_TITTEL)
	 .dokumentInfoId(dokumentInfoId++)
	 .originalJournalpost(originalJournalpost)
	 .build())
	 .build())
	 .build();
	 originalJournalpost.getJournalpostDokumentInfoRelasjoner().add(vedlegg.getThisJournalpostDokumentInfoRelasjon());

	 return  vedlegg;
	 }

	 public static Journalpost opprettKnyttetVedleggForIT(Boolean sletteStatus) {
	 return getBaseJournalpostBuilder()
	 .opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
	 .dokumentInfoRelasjoner(
	 getBaseJournalpostDokumentInfoRelasjonBuilder(TilknyttetJournalpostSomCode.VEDLEGG)
	 .tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
	 .dokumentInfo(opprettDokumentInfoBuilder(sletteStatus, VEDLEGG_TITTEL)
	 .build())
	 .build())
	 .build();
	 }

	 public static Journalpost opprettVedleggForEnhetsTest(Boolean sletteStatus) {
	 return getBaseJournalpostBuilder()
	 .journalpostId(journalpostId++)
	 .dokumentInfoRelasjoner(
	 getBaseJournalpostDokumentInfoRelasjonBuilder(TilknyttetJournalpostSomCode.VEDLEGG)
	 .journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++)
	 .dokumentInfo(opprettDokumentInfoBuilder(sletteStatus, VEDLEGG_TITTEL)
	 .dokumentInfoId(dokumentInfoId++)
	 .build())
	 .build())
	 .build();
	 }


	 private static JournalpostDokumentInfoRelasjonBuilder getBaseJournalpostDokumentInfoRelasjonBuilder(TilknyttetJournalpostSomCode tilknyttetSom, Boolean enhetstest) {
	 return getBaseJournalpostDokumentInfoRelasjonBuilder(tilknyttetSom)
	 .journalpostDokumentInfoRelasjonId(journalpostDokumentinfoRelasjonId++);
	 }

	 private static DokumentInfoBuilder opprettDokumentInfoBuilder(Boolean sletteStatus, String tittel, Boolean enhetstest) {
	 return opprettDokumentInfoBuilder(sletteStatus, tittel)
	 .dokumentInfoId(dokumentInfoId++);
	 }

	 private static DokumentInfoBuilder opprettDokumentInfoBuilder(Boolean sletteStatus, String tittel, Journalpost originalJournalpost) {
	 return opprettDokumentInfoBuilder(sletteStatus, tittel)
	 .originalJournalpost(originalJournalpost);
	 }

	 private static DokumentInfoBuilder opprettDokumentInfoBuilder(Boolean sletteStatus, String tittel, Boolean enhetstest, Journalpost originalJournalpost) {
	 return opprettDokumentInfoBuilder(sletteStatus, tittel, enhetstest)
	 .originalJournalpost(originalJournalpost);
	 }
	 */


}
