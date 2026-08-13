package no.nav.dokarkiv.journalpost.v1.util;

import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.api.Fagsaksystem;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static no.nav.dokarkiv.core.util.TestdataFactory.createBruker;
import static no.nav.dokarkiv.core.util.TestdataFactory.createKryssreferanse;
import static no.nav.dokarkiv.core.util.TestdataFactory.createSaksrelasjon;


public class TestDataUtils {

	public static final long JOURNALPOST_ID = 1234L;
	public static final long JOURNALPOST_ID_O = 12345L;

	public static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	public static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	public static final String AVSENDER_MOTTAKER_ID = "02016126007";
	public static final AvsenderMottakerIdTypeCode AVSENDER_MOTTAKER_ID_TYPE = AvsenderMottakerIdTypeCode.FNR;
	public static final String DOKUMENT_INFO_TITTEL = "TITTEL";
	public static final String DOKUMENT_TYPE_ID = "0000001";
	public static final Long SAK_ID = 1232131233L;
	public static final String PSAK_ID = "090909090";
	public static final String FIL_NAVN = "navn";
	public static final String TILLEGGOPPLYSNINGER_KEY = "tillegg";
	public static final String TILLEGGOPPLYSNINGER_VAL = "tillegg_verdi";
	public static final byte[] FIL = "Test dokument".getBytes();
	public static final byte[] FIL_DUMMY_KASSERT = "Test kassert dummy dokument dummy".getBytes();
	public static final Integer ANTALL_RETUR = 3;
	public static final String INNHOLD = "innhold";
	public static final String DOKUMENTINFO_ID1 = "1234567";
	public static final String DOKUMENT_TITTEL1 = "dokumentTittel1";
	public static final String AVSENDER_ID_PERSON = "12345678910";
	public static final String AVSENDER_NAVN = "avsenderNavn";
	public static final String BRUKER_ID_PERSON = "10987654321";
	public static final String BRUKER_ID_ORGANISASJON = "123456789";
	public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2017, 2, 3, 10, 37, 30);
	public static final LocalDateTime MOTTAT_DATO = LocalDateTime.of(2017, 2, 3, 10, 37, 30);
	public static final String JOURNALFOERENDE_ENHET = "4000";
	public static final String KANALREFERANSE_ID = "kanalreferansId";
	public static final String AKTOER_ID = "1234567890123";
	public static final String FAGSAK_ID = "123abc";
	public static final String CONSUMER_ID = "consumerId";
	public static final String TEMA_FOR = "FOR";
	public static final String TEMA_SER = "SER";
	public static final String TEMA_PEN = "PEN";
	public static final String TEMA_UFO = "UFO";
	public static final String TEMA_TIL = "TIL";
	public static final String TEMA_SYM = "SYM";
	public static final String TEMA_KTR = "KTR";

	private static final String DOKUMNETTYPE_ID1 = "dokumenttypeID1";
	private static final String BREVKODE1 = "brevkode1";
	private static final String SKANNETINNHOLD_ID1 = "78547541";
	private static final String VEDLEGGINNHOLD1 = "vedlegginnhold1";
	private static final String DOKUMENTINFO_ID2 = "74545455";
	private static final String DOKUMNETTYPE_ID2 = "dokumenttypeID2";
	private static final String BREVKODE2 = "brevkode2";
	private static final String DOKUMENT_TITTEL2 = "dokumentTittel2";
	private static final String SKANNETINNHOLD_ID2 = "9874564";
	private static final String VEDLEGGINNHOLD2 = "vedlegginnhold2";
	private static final String SKANNETINNHOLD_ID3 = "6875454564";
	private static final String VEDLEGGINNHOLD3 = "vedlegginnhold3";

	public static final JournalStatusCode UNDER_ARBEID = JournalStatusCode.D;
	private static final JournalpostTypeCode UTGAAENDE_DOKUMENT = JournalpostTypeCode.U;


	public static Journalpost createEnkelJournalpost() {
		Journalpost journalpost = Journalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.journalstatus(JournalStatusCode.J)
				.avsenderMottakerId(AVSENDER_ID_PERSON)
				.avsenderMottakerIdType(AvsenderMottakerIdTypeCode.FNR)
				.avsenderMottaker(AVSENDER_NAVN)
				.journalposttype(JournalpostTypeCode.I)
				.fagomrade(FagomradeCode.BID)
				.innhold(INNHOLD)
				.kanalReferanseId(KANALREFERANSE_ID)
				.mottakskanal(MottaksKanalCode.ALTINN)
				.mottattDato(LOCAL_DATE_TIME)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.saksrelasjon(Saksrelasjon.builder()
						.sakId(12345L)
						.fagsystem(no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22)
						.build())
				.build();

		journalpost.addBruker(createPersonBruker());
		journalpost.addBruker(createOrganisasjonBruker());
		journalpost.addJournalpostDokumentInfoRelasjon(createJournalpostDokumentinfoRelasjon1());
		journalpost.addJournalpostDokumentInfoRelasjon(createJournalpostDokumentinfoRelasjon2());

		return journalpost;
	}

	public static Journalpost createEnkelJournalpost(JournalStatusCode journalpostStatus, JournalpostTypeCode journalpostType) {
		return Journalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.journalstatus(journalpostStatus)
				.journalposttype(journalpostType)
				.build();
	}

	public static Journalpost createJournalpostForOppdatering() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(JournalStatusCode.M)
				.avsenderMottakerId(AVSENDER_ID_PERSON)
				.avsenderMottaker(AVSENDER_NAVN)
				.journalposttype(JournalpostTypeCode.I)
				.fagomrade(FagomradeCode.BID)
				.innhold(INNHOLD)
				.kanalReferanseId(KANALREFERANSE_ID)
				.mottakskanal(MottaksKanalCode.ALTINN)
				.mottattDato(MOTTAT_DATO)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.saksrelasjon(Saksrelasjon.builder()
						.sakId(12345L)
						.fagsystem(no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22)
						.build())
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.build();

		journalpost.addBruker(createPersonBruker());
		journalpost.addJournalpostDokumentInfoRelasjon(createJournalpostDokumentinfoRelasjon1());
		return journalpost;
	}

	public static Sak createGenerellSak() {
		return Sak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_SYM)
				.applikasjon(no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22.name())
				.opprettetAv(CONSUMER_ID)
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
	}

	public static Sak createFagsak() {
		return Sak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_TIL)
				.applikasjon(Fagsaksystem.AO01.name())
				.fagsakNr(FAGSAK_ID)
				.opprettetAv(CONSUMER_ID)
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
	}

	private static JournalpostDokumentInfoRelasjon createJournalpostDokumentinfoRelasjon1() {
		return JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(DokumentInfo.builder()
						.dokumentInfoId(Long.parseLong(DOKUMENTINFO_ID1))
						.dokumenttypeId(DOKUMNETTYPE_ID1)
						.brevkode(BREVKODE1)
						.tittel(DOKUMENT_TITTEL1)
						.kategori(DokumentKategoriCode.ELEKTRONISK_DIALOG)
						.fildetaljerListe(new HashSet<>(Arrays.asList(
								FilDetaljer.builder().filtype(FilTypeCode.XML).variantFormat(VariantFormatCode.ORIGINAL).build(),
								FilDetaljer.builder().filtype(FilTypeCode.PDFA).variantFormat(VariantFormatCode.ARKIV).build(),
								FilDetaljer.builder().filtype(FilTypeCode.PDFA).variantFormat(VariantFormatCode.SLADDET).build())))
						.skannetInnholdListe(new HashSet<>(Arrays.asList(
								SkannetInnhold.builder().skannetInnholdId(Long.parseLong(SKANNETINNHOLD_ID1)).vedleggInnhold(VEDLEGGINNHOLD1).build(),
								SkannetInnhold.builder().skannetInnholdId(Long.parseLong(SKANNETINNHOLD_ID2)).vedleggInnhold(VEDLEGGINNHOLD2).build())))
						.build())
				.build();
	}

	private static JournalpostDokumentInfoRelasjon createJournalpostDokumentinfoRelasjon2() {
		return JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(DokumentInfo.builder()
						.dokumentInfoId(Long.parseLong(DOKUMENTINFO_ID2))
						.dokumenttypeId(DOKUMNETTYPE_ID2)
						.brevkode(BREVKODE2)
						.tittel(DOKUMENT_TITTEL2)
						.kategori(DokumentKategoriCode.FORVALTNINGSNOTAT)
						.fildetaljerListe(new HashSet<>(Arrays.asList(
								FilDetaljer.builder().filtype(FilTypeCode.PDFA).variantFormat(VariantFormatCode.ARKIV).build(),
								FilDetaljer.builder().filtype(FilTypeCode.PDFA).variantFormat(VariantFormatCode.SLADDET).build())))
						.skannetInnholdListe(new HashSet<>(Arrays.asList(
								SkannetInnhold.builder().skannetInnholdId(Long.parseLong(SKANNETINNHOLD_ID3)).vedleggInnhold(VEDLEGGINNHOLD3).build())))
						.build())
				.build();
	}

	private static Bruker createPersonBruker() {
		return Bruker.builder()
				.brukerId(BRUKER_ID_PERSON)
				.brukerType(BrukerTypeCode.PERSON)
				.build();
	}

	private static Bruker createOrganisasjonBruker() {
		return Bruker.builder()
				.brukerId(BRUKER_ID_ORGANISASJON)
				.brukerType(BrukerTypeCode.ORGANISASJON)
				.build();
	}

	public static Journalpost createJournalpostUnderArbeid() {
		Journalpost journalpost = Journalpost.builder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottakerIdType(AVSENDER_MOTTAKER_ID_TYPE)
				.dokumentDato(LocalDateTime.now())
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalstatus(UNDER_ARBEID)
				.journalposttype(UTGAAENDE_DOKUMENT)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.antallRetur(ANTALL_RETUR).build();

		journalpost.addBruker(createBruker());
		journalpost.addKryssReferanse(createKryssreferanse());
		journalpost.setSaksrelasjon(createSaksrelasjon(SAK_ID, journalpost));
		journalpost.setTilleggsopplysninger(createTilleggsopplysninger());
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost));
		return journalpost;
	}

	public static Map<String, String> createTilleggsopplysninger() {
		Map<String, String> tilleggsopplysninger = new HashMap<>();
		tilleggsopplysninger.put(TILLEGGOPPLYSNINGER_KEY, TILLEGGOPPLYSNINGER_VAL);
		return tilleggsopplysninger;
	}


	public static JournalpostDokumentInfoRelasjon createHoveddokumentRelasjon(Journalpost journalpost) {
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setOriginalJournalpost(journalpost);

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		return journalpostDokumentInfoRelasjon;
	}

	public static JournalpostDokumentInfoRelasjon createVedleggRelasjon(Journalpost journalpost, DokumentInfo dokumentInfo) {

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);

		journalpost.addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);
		return journalpostDokumentInfoRelasjon;
	}

	public static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(DOKUMENT_INFO_TITTEL)
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.build();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.ARKIV));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.PRODUKSJON));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold());
		dokumentInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		dokumentInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		return dokumentInfo;
	}

	public static SkannetInnhold createSkannetInnhold() {
		SkannetInnhold skannetInnhold = SkannetInnhold.builder()
				.dokumenttypeid(DOKUMENT_TYPE_ID)
				.build();
		skannetInnhold.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return skannetInnhold;
	}

	public static FilDetaljer createFildetaljerOgFil(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode) {
		return createFildetaljerOgFil(dokumentInfo, variantFormatCode, FilDetaljer.generateUuid());
	}


	public static FilDetaljer createFildetaljerOgFil(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, String filUuid) {
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.dokumentInfo(dokumentInfo)
				.fileContent(FIL)
				.filnavn(FIL_NAVN)
				.filtype(FilTypeCode.PDF)
				.filUuid(filUuid)
				.variantFormat(variantFormatCode)
				.build();
		filDetaljer.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return filDetaljer;
	}


	public static Journalpost createJournalpostIngaaende() {
		return Journalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.journalposttype(JournalpostTypeCode.I)
				.journalstatus(JournalStatusCode.FL)
				.fagomrade(FagomradeCode.AAP)
				.innhold(INNHOLD)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.build();
	}


	public static DokumentInfo createDokumentInfoWithLikJournalpost() {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentInfoId(Long.valueOf(DOKUMENTINFO_ID1))
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.originalJournalpost(Journalpost.builder().journalpostId(JOURNALPOST_ID).build())
				.tittel(DOKUMENT_INFO_TITTEL)
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.build();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.ARKIV));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.PRODUKSJON));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold());
		dokumentInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		dokumentInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		return dokumentInfo;
	}

	public static JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjonHovedDok() {

		JournalpostDokumentInfoRelasjon jpDokRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(DokumentInfo.builder()
						.dokumentInfoId(Long.valueOf(DOKUMENTINFO_ID1))
						.tittel(DOKUMENT_TITTEL1)
						.build())
				.tilknyttetAvNavn("Test Testen")
				.build();

		jpDokRelasjon.setJournalpost(createJournalpostIngaaende());
		jpDokRelasjon.setOpprettetKildeNavn(OPPRETTET_AV_NAVN);


		return jpDokRelasjon;
	}


}
