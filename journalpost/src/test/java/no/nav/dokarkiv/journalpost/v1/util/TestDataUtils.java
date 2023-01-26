package no.nav.dokarkiv.journalpost.v1.util;

import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TestDataUtils {

	public static final long JOURNALPOST_ID = 1234L;
	public static final long JOURNALPOST_ID_O = 12345L;

	public static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	public static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	public static final String AVSENDER_MOTTAKER_ID = "02016126007";
	public static final AvsenderMottakerIdTypeCode AVSENDER_MOTTAKER_ID_TYPE = AvsenderMottakerIdTypeCode.FNR;
	public static final String BRUKER_ID = "123213";
	public static final String KRYSSREFERANSE_ID = "123213";
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


	public static final JournalStatusCode UNDER_ARBEID = JournalStatusCode.D;
	private static final JournalpostTypeCode UTGAAENDE_DOKUMENT = JournalpostTypeCode.U;


	public static Journalpost createJournalpostUnderArbeid() {
		Journalpost journalpost = Journalpost.builder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottakerIdType(AVSENDER_MOTTAKER_ID_TYPE)
				.dokumentDato(new Date())
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalstatus(UNDER_ARBEID)
				.journalposttype(UTGAAENDE_DOKUMENT)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.antallRetur(ANTALL_RETUR).build();

		journalpost.addBruker(createBruker());
		journalpost.addKryssReferanse(createKryssreferanse());
		journalpost.setSaksrelasjon(createSaksrelasjon(journalpost));
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

	public static Saksrelasjon createSaksrelasjon(Journalpost journalpost) {
		Saksrelasjon saksrelasjon = Saksrelasjon.builder()
				.fagsystem(FagsystemCode.FS22)
				.sakId(SAK_ID)
				.saknrfk(SAK_ID.toString())
				.journalpost(journalpost)
				.build();
		saksrelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return saksrelasjon;
	}

	public static Bruker createBruker() {
		Bruker bruker = Bruker.builder()
				.brukerType(BrukerTypeCode.PERSON)
				.brukerId(BRUKER_ID)
				.build();
		bruker.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return bruker;
	}

	public static Kryssreferanse createKryssreferanse() {
		Kryssreferanse kryssreferanse = Kryssreferanse.builder()
				.referanseType(ReferanseTypeCode.SPOERSMAAL)
				.referanseId(KRYSSREFERANSE_ID)
				.referanseNr(1L)
				.build();
		kryssreferanse.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return kryssreferanse;
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
