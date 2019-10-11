package no.nav.dokarkiv.journalpost.v1.util;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttJournalpostRequest;

import java.time.LocalDateTime;


public class TestDataUtils {

	public static final long JOURNALPOST_ID = 1234L;
	public static final long JOURNALPOST_ID_O = 12345L;

	public static final String AVSENDER_NAVN = "avsenderNavn";
	public static final String AVSENDER_NAVN_ORGANISASJON = "avsenderNavn_org";
	public static final String AVSENDER_NAVN_HELSEPERSONELLNR = "avsenderNavn_hprnr";
	public static final String AVSENDER_NAVN_UTLORGANISASJON = "avsenderNavn_utl_org";
	public static final String AVSENDER_ID_PERSON = "***gammelt_fnr***";
	public static final String AVSENDER_ID_ORGANISASJON = "123456789";
	public static final String AVSENDER_ID_HELSEPERSONELLNR = "123456789";
	public static final String AVSENDER_ID_UTLORGANISASJON = "123456789";


	public static final String BRUKER_ID_PERSON = "***gammelt_fnr***";
	public static final String BRUKER_ID_ORGANISASJON = "987654321";
	public static final String SAK_ID = "sakId";
	public static final String INNHOLD = "innhold";
	public static final String KANALREFERANSE_ID = "kanalreferansId";
	public static final String DATO_MOTTATT = "2017-02-03T11:37:30";
	public static final String JOURNALFOERENDE_ENHET = "journalfoerendeEnhet";
	public static final String DOKUMENTINFO_ID1 = "1234567";
	public static final String DOKUMNETTYPE_ID1 = "dokumenttypeID1";
	public static final String BREVKODE1 = "brevkode1";
	public static final String DOKUMENT_TITTEL1 = "dokumentTittel1";
	public static final String DOKUMENT_TITTEL_UPDATE = "dokumentTittel_UPDATE";
	public static final String SKANNETINNHOLD_ID1 = "78547541";
	public static final String VEDLEGGINNHOLD1 = "vedlegginnhold1";
	public static final String DOKUMENTINFO_ID2 = "74545455";
	public static final String DOKUMNETTYPE_ID2 = "dokumenttypeID2";
	public static final String DOKUMNETTYPE_ID_UPDATE = "dokumenttypeID_UPDATE";
	public static final String BREVKODE2 = "brevkode2";
	public static final String BREVKODE_UPDATE = "brevkode_Update";
	public static final String DOKUMENT_TITTEL2 = "dokumentTittel2";
	public static final String SKANNETINNHOLD_ID2 = "9874564";
	public static final String VEDLEGGINNHOLD2 = "vedlegginnhold2";
	public static final String SKANNETINNHOLD_ID3 = "6875454564";
	public static final String VEDLEGGINNHOLD3 = "vedlegginnhold3";
	public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2017, 2, 3, 10, 37, 30);
	public static final String OPPRETTET_AV_NAVN = "Sak S. Behandler";
	public static final String TEMA_FOR = "FOR";
	public static final String TEMA_SER = "SER";
	public static final String BEHANDLINGSTEMA = "ab0001";
	public static final String AVSENDER_MOTTAKER_LAND = "Legoland";
	public static final String AVSENDER_MOTTAKER_UTLAND = "Utland";
	public static final String KANAL_NAVNO = "NAV_NO";
	public static final String DOKUMENTKATEGORI_SED = "SED";
	public static final String FILTYPE_PDF = "PDF";
	public static final String FILTYPE_XML = "XML";
	public static final String VARIANTFORMAT_ARKIV = "ARKIV";
	public static final String VARIANTFORMAT_ORIGINAL = "ORIGINAL";
	public static final byte[] FYSISK_DOKUMENT = "DOKUMENT".getBytes();
	public static final byte[] FYSISK_DOKUMENT_2 = "DOKUMENT_2".getBytes();
	public static final String TILLEGGSOPPLYSNING_NOKKEL = "noekkel";
	public static final String TILLEGGSOPPLYSNING_VERDI = "verdi";
	public static final String FILNAVN = "filnavn";

	public static final JournalStatusCode UNDER_ARBEID = JournalStatusCode.D;
	private static final JournalpostTypeCode UTGAAENDE_DOKUMENT = JournalpostTypeCode.U;

	public static Journalpost createJournalpost(){
		return Journalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.journalposttype(UTGAAENDE_DOKUMENT)
				.journalstatus(UNDER_ARBEID)
				.fagomrade(FagomradeCode.AAP)
				.innhold(INNHOLD)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.build();
	}

	public static Journalpost createJournalpostIngaaende(){
		return Journalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.journalposttype(JournalpostTypeCode.I)
				.journalstatus(JournalStatusCode.FL)
				.fagomrade(FagomradeCode.AAP)
				.innhold(INNHOLD)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.build();
	}


	public static JournalpostDokumentInfoRelasjon JournalpostDokumentInfoRelasjon(){

		return JournalpostDokumentInfoRelasjon().builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(DokumentInfo.builder()
						.dokumentInfoId(Long.valueOf(DOKUMENTINFO_ID1))
						.tittel(DOKUMENT_TITTEL1)
						.build())
				.tilknyttetAvNavn("Test Testen")
				.build();

	}




	public static DokumentInfo createDokumentInfo(){

		return DokumentInfo.builder()
				.dokumentInfoId(Long.valueOf(DOKUMENTINFO_ID1))
				.originalJournalpost(Journalpost.builder()
						.journalpostId(JOURNALPOST_ID_O)
						.innhold(INNHOLD)
						.journalstatus(UNDER_ARBEID)
						.journalposttype(UTGAAENDE_DOKUMENT)
						.build())
				.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
				.build();
	}

	public static FjernVedleggTilknyttJournalpostRequest createFjernVedleggTilknyttJournalpostRequest(){
		return FjernVedleggTilknyttJournalpostRequest.builder()
				.dokumentId(DOKUMENTINFO_ID1)
				.build();
	}


}
