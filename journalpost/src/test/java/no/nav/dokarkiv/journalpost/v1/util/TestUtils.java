package no.nav.dokarkiv.journalpost.v1.util;

import no.nav.dokarkiv.core.consumer.ereg.EregResponse;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import no.nav.dokarkiv.journalpost.v1.api.knytttilannensak.KnyttTilAnnenSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22;
import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.AO01;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.AvsenderMottakerUpdater.DELETE_MARKER;

public class TestUtils {

	public static final long JOURNALPOST_ID = 1234L;
	public static final String AVSENDER_NAVN = "avsenderNavn";
	public static final String AVSENDER_NAVN_ORGANISASJON = "avsenderNavn_org";
	public static final String AVSENDER_NAVN_HELSEPERSONELLNR = "avsenderNavn_hprnr";
	public static final String AVSENDER_NAVN_UTLORGANISASJON = "avsenderNavn_utl_org";
	public static final String AVSENDER_ID_PERSON = "12345678910";
	public static final String AVSENDER_ID_ORGANISASJON = "123456789";
	public static final String AVSENDER_ID_HELSEPERSONELLNR = "123456789";
	public static final String AVSENDER_ID_UTLORGANISASJON = "123456789";

	public static final LocalDateTime MOTTAT_DATO = LocalDateTime.of(2017, 2, 3, 10, 37, 30);
	public static final String BRUKER_ID_PERSON = "10987654321";
	public static final String BRUKER_ID_ORGANISASJON = "123456789";
	public static final Long SAK_ID = 12345L;
	public static final String FAGSAK_ID = "123abc";
	public static final String PENSJON_FAGSAK_ID = "54321";
	public static final String ARKIVSAKSNUMMER = "1234567890";
	public static final String INNHOLD = "innhold";
	public static final String KANALREFERANSE_ID = "kanalreferansId";
	public static final LocalDateTime DATO_DOKUMENT = LocalDateTime.now().minusDays(3);
	public static final LocalDateTime DATO_MOTTATT = LocalDateTime.now().minusDays(2);
	public static final LocalDateTime DATO_MOTTATT_1 = LocalDateTime.now().minusDays(1);
	public static final String JOURNALFOERENDE_ENHET = "4000";
	public static final String JOURNALFOERENDE_ENHET_UGYLDIG = "40000";
	public static final String JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES = "    ";
	public static final String DOKUMENTINFO_ID1 = "1234567";
	public static final String DOKUMNETTYPE_ID1 = "dokumenttypeID1";
	public static final String BREVKODE1 = "brevkode1";
	public static final String BREVKODE_4936 = "4936";
	public static final String DOKUMENT_TITTEL1 = "dokumentTittel1";
	public static final String SKANNETINNHOLD_ID1 = "78547541";
	public static final String VEDLEGGINNHOLD1 = "vedlegginnhold1";
	public static final String DOKUMENTINFO_ID2 = "74545455";
	public static final String DOKUMNETTYPE_ID2 = "dokumenttypeID2";
	public static final String BREVKODE2 = "brevkode2";
	public static final String DOKUMENT_TITTEL2 = "dokumentTittel2";
	public static final String SKANNETINNHOLD_ID2 = "9874564";
	public static final String VEDLEGGINNHOLD2 = "vedlegginnhold2";
	public static final String SKANNETINNHOLD_ID3 = "6875454564";
	public static final String VEDLEGGINNHOLD3 = "vedlegginnhold3";
	public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2017, 2, 3, 10, 37, 30);
	public static final String OPPRETTET_AV_NAVN = "Sak S. Behandler";
	public static final String TEMA_FOR = "FOR";
	public static final String TEMA_SER = "SER";
	public static final String TEMA_PEN = "PEN";
	public static final String TEMA_UFO = "UFO";
	public static final String TEMA_TIL = "TIL";
	public static final String TEMA_SYM = "SYM";
	public static final String TEMA_KTR = "KTR";
	public static final String BEHANDLINGSTEMA = "ab9999";
	public static final String AVSENDER_MOTTAKER_LAND = "Legoland";
	public static final String AVSENDER_MOTTAKER_UTLAND = "Utland";
	public static final String KANAL_NAVNO = "NAV_NO";
	public static final String KANAL_ALTINN = "ALTINN";
	public static final String DOKUMENTKATEGORI_SED = "SED";
	public static final String DOKUMENTKATEGORI_SOK = "SOK";
	public static final String DOKUMENTKATEGORI_UGYLDIG = "UGYLDIG";
	public static final String FILTYPE_PDF = "PDF";
	public static final String FILTYPE_PDFA = "PDFA";
	public static final String FILTYPE_XML = "XML";
	public static final String FILTYPE_XLSX = "XLSX";
	public static final String FILTYPE_UGYLDIG = "UGYLDIG";
	public static final String VARIANTFORMAT_ARKIV = "ARKIV";
	public static final String VARIANTFORMAT_ORIGINAL = "ORIGINAL";
	public static final String VARIANTFORMAT_UGYLDIG = "UGYLDIG";
	public static final byte[] FYSISK_DOKUMENT = Base64.getDecoder().decode("JVBERi0xLjcNCiWhs8U=");
	public static final byte[] FYSISK_DOKUMENT_WITH_INVALID_MAGIC_NUMBER = Base64.getDecoder().decode("/9j/4AAQSkZJRgABAQEAZA==");
	public static final byte[] FYSISK_DOKUMENT_2 = "DOKUMENT_2".getBytes();
	public static final byte[] XLSX_DOKUMENT = Base64.getDecoder().decode("UEsDBA==");
	public static final String TILLEGGSOPPLYSNING_NOKKEL = "noekkel";
	public static final String TILLEGGSOPPLYSNING_VERDI = "verdi";
	public static final String FILNAVN = "filnavn";
	public static final String FILNAVN_PDF = "filnavn.pdf";
	public static final String FILNAVN_XML = "filnavn.xml";
	public static final String FILNAVN_VEDLEGG = "vedlegg.pdf";
	public static final String BATCHNAVN = "batchnavn";

	public static final String CONSUMER_ID = "consumerId";
	public static final String AKTOER_ID = "1234567890123";
	public static final String FAIL_AKTOER_ID = "9343877893406";
	public static final String FNR = "01010199999";
	public static final String FNR_2 = "01010188888";
	public static final String FNR_UGYLDIG = "12345678901";

	public static final LocalDateTime FORTID = LocalDateTime.now().minusDays(1);
	public static final LocalDateTime FREMTID = LocalDateTime.now().plusDays(1);
	public static final LocalDate FORTID_DATO = LocalDate.now().minusDays(1);
	public static final LocalDate FREMTID_DATO = LocalDate.now().plusDays(1);

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
						.sakId(SAK_ID)
						.fagsystem(FS22)
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
						.sakId(SAK_ID)
						.fagsystem(FS22)
						.build())
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.build();

		journalpost.addBruker(createPersonBruker());
		journalpost.addJournalpostDokumentInfoRelasjon(createJournalpostDokumentinfoRelasjon1());
		return journalpost;
	}

	public static JournalpostDokumentInfoRelasjon createJournalpostDokumentinfoRelasjon1() {
		return JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(DokumentInfo.builder()
						.dokumentInfoId(Long.parseLong(DOKUMENTINFO_ID1))
						.dokumenttypeId(DOKUMNETTYPE_ID1)
						.brevkode(BREVKODE1)
						.tittel(DOKUMENT_TITTEL1)
						.kategori(DokumentKategoriCode.ELEKTRONISK_DIALOG)
						.fildetaljerListe(new HashSet<>(Arrays.asList(FilDetaljer.builder()
										.filtype(FilTypeCode.XML)
										.variantFormat(VariantFormatCode.ORIGINAL)
										.build(),
								FilDetaljer.builder()
										.filtype(FilTypeCode.PDFA)
										.variantFormat(VariantFormatCode.ARKIV)
										.build(),
								FilDetaljer.builder()
										.filtype(FilTypeCode.PDFA)
										.variantFormat(VariantFormatCode.SLADDET)
										.build())))
						.skannetInnholdListe(new HashSet<>(Arrays.asList(SkannetInnhold.builder()
										.skannetInnholdId(Long.parseLong(SKANNETINNHOLD_ID1))
										.vedleggInnhold(VEDLEGGINNHOLD1)
										.build(),
								SkannetInnhold.builder()
										.skannetInnholdId(Long.parseLong(SKANNETINNHOLD_ID2))
										.vedleggInnhold(VEDLEGGINNHOLD2)
										.build())))
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
						.fildetaljerListe(new HashSet<>(Arrays.asList(FilDetaljer.builder()
										.filtype(FilTypeCode.PDFA)
										.variantFormat(VariantFormatCode.ARKIV)
										.build(),
								FilDetaljer.builder()
										.filtype(FilTypeCode.PDFA)
										.variantFormat(VariantFormatCode.SLADDET)
										.build())))
						.skannetInnholdListe(new HashSet<>(Arrays.asList(SkannetInnhold.builder()
								.skannetInnholdId(Long.parseLong(SKANNETINNHOLD_ID3))
								.vedleggInnhold(VEDLEGGINNHOLD3)
								.build())))
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

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequest() {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestSak(Sak sak) {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.bruker(createBrukerPerson())
				.sak(sak)
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithDatoRetur(LocalDate datoRetur) {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.datoRetur(datoRetur)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}


	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithDatoMottat(LocalDateTime datoMottatt) {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.datoMottatt(datoMottatt)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestUtenDatoMottat() {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}


	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithoutAvsenderMottakerId() {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottaker(null, DELETE_MARKER, AvsenderMottakerIdType.FNR))
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithoutAvsenderMottaker() {
		return OppdaterJournalpostRequest.builder()
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithoutWrongAvsenderMottakerId() {
		return createPutOppdaterJournalpostRequestWithAvsenderMottaker(createAvsenderMottaker(AVSENDER_NAVN, "", AvsenderMottakerIdType.FNR));
	}

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(avsenderMottaker)
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostAvsenderMottakerKunLandRequest() {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(AvsenderMottaker.builder().land(AVSENDER_MOTTAKER_UTLAND).build())
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}

	public static AvsenderMottaker createAvsenderMottakerPerson() {
		return createAvsenderMottaker(AVSENDER_NAVN, AVSENDER_ID_PERSON, AvsenderMottakerIdType.FNR);
	}

	public static AvsenderMottaker createAvsenderMottakerPersonWithoutNavnAndIdType() {
		return createAvsenderMottaker(null, AVSENDER_ID_PERSON, null);
	}

	public static AvsenderMottaker createAvsenderMottakerPersonWithoutNavn() {
		return createAvsenderMottaker(null, AVSENDER_ID_PERSON, AvsenderMottakerIdType.FNR);
	}

	public static AvsenderMottaker createAvsenderMottakerPersonWithoutIdAndNavn() {
		return createAvsenderMottaker(null, null, ORGNR);
	}

	public static AvsenderMottaker createAvsenderMottaker(String id, AvsenderMottakerIdType idType) {
		return createAvsenderMottaker(AVSENDER_NAVN, id, idType);
	}

	public static AvsenderMottaker createAvsenderMottaker(String navn, String id, AvsenderMottakerIdType idType) {
		return createAvsenderMottaker(navn, id, idType, AVSENDER_MOTTAKER_LAND);
	}

	public static AvsenderMottaker createAvsenderMottaker(String navn, String id, AvsenderMottakerIdType idType, String land) {
		return AvsenderMottaker.builder()
				.navn(navn)
				.id(id)
				.idType(idType)
				.land(land)
				.build();
	}

	public static AvsenderMottaker createAvsenderMottakerOrganisasjon() {
		return AvsenderMottaker.builder()
				.navn(AVSENDER_NAVN_ORGANISASJON)
				.id(AVSENDER_ID_ORGANISASJON)
				.idType(AvsenderMottakerIdType.ORGNR)
				.land(AVSENDER_MOTTAKER_LAND)
				.build();
	}

	public static AvsenderMottaker createAvsenderMottakerHelsepersonell() {
		return AvsenderMottaker.builder()
				.navn(AVSENDER_NAVN_HELSEPERSONELLNR)
				.id(AVSENDER_ID_HELSEPERSONELLNR)
				.idType(AvsenderMottakerIdType.HPRNR)
				.land(AVSENDER_MOTTAKER_LAND)
				.build();
	}

	public static AvsenderMottaker createAvsenderMottakerUtlandOrganisasjon() {
		return AvsenderMottaker.builder()
				.navn(AVSENDER_NAVN_UTLORGANISASJON)
				.id(AVSENDER_ID_UTLORGANISASJON)
				.idType(AvsenderMottakerIdType.UTL_ORG)
				.land(AVSENDER_MOTTAKER_LAND)
				.build();
	}

	public static AvsenderMottaker createAvsenderMottakerOrganisasjonWithoutNavn() {
		return AvsenderMottaker.builder()
				.id(AVSENDER_ID_ORGANISASJON)
				.idType(ORGNR)
				.build();
	}

	public static no.nav.dokarkiv.journalpost.v1.api.Bruker createBrukerPerson() {
		return no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
				.idType(BrukerIdType.FNR)
				.id(BRUKER_ID_PERSON)
				.build();
	}

	public static Sak createSak() {
		return Sak.builder()
				.arkivsaksnummer(SAK_ID.toString())
				.arkivsaksystem(Arkivsaksystem.GSAK)
				.build();
	}

	private static List<Tilleggsopplysning> createTilleggsopplysninger() {
		return Arrays.asList(Tilleggsopplysning.builder().nokkel("nokkel").verdi("verdi").build());
	}

	private static List<no.nav.dokarkiv.journalpost.v1.api.DokumentInfo> createDokumentInfos() {
		return Arrays.asList(
				no.nav.dokarkiv.journalpost.v1.api.DokumentInfo.builder()
						.brevkode(BREVKODE1)
						.dokumentInfoId(DOKUMENTINFO_ID1)
						.tittel(DOKUMENT_TITTEL1)
						.build(),
				no.nav.dokarkiv.journalpost.v1.api.DokumentInfo.builder()
						.brevkode(BREVKODE2)
						.dokumentInfoId(DOKUMENTINFO_ID2)
						.tittel(DOKUMENT_TITTEL2)
						.build()
		);
	}

	public static KnyttTilAnnenSakRequest createKnyttTilAnnenSakRequest(
			String sakstype,
			String fagsakId,
			String fagsaksystem,
			String tema,
			BrukerIdType brukerIdType,
			String brukerId,
			String journalfoerendeEnhet,
			List<Long> dokumenter) {
		no.nav.dokarkiv.journalpost.v1.api.Bruker bruker = no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
				.idType(brukerIdType)
				.id(brukerId)
				.build();

		return KnyttTilAnnenSakRequest.builder()
				.sakstype(sakstype)
				.fagsakId(fagsakId)
				.fagsaksystem(fagsaksystem)
				.tema(tema)
				.bruker(bruker)
				.journalfoerendeEnhet(journalfoerendeEnhet)
				.dokumenter(dokumenter)
				.build();
	}

	public static OpprettJournalpostRequest createRequest(JournalpostType journalpostType) {
		return createRequest(journalpostType, null);
	}

	public static OpprettJournalpostRequest createRequest(JournalpostType journalpostType, String journalfoerendeEnhet) {
		return createRequest(journalpostType, journalfoerendeEnhet, SAK_ID.toString());
	}

	public static OpprettJournalpostRequest createRequest(JournalpostType journalpostType, String journalfoerendeEnhet, String sakId) {
		return createBaseRequest(journalpostType, sakId)
				.journalfoerendeEnhet(journalfoerendeEnhet)
				.dokumenter(Arrays.asList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(Arrays.asList(DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.variantformat(VARIANTFORMAT_ARKIV)
												.fysiskDokument(FYSISK_DOKUMENT)
												.batchnavn(BATCHNAVN)
												.build(),
										DokumentVariant.builder()
												.filtype(FILTYPE_XML)
												.variantformat(VARIANTFORMAT_ORIGINAL)
												.filnavn(FILNAVN)
												.fysiskDokument(FYSISK_DOKUMENT_2)
												.batchnavn(BATCHNAVN)
												.build()))
								.build(),
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL2)
								.brevkode(BREVKODE2)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.rekkefoelge(2)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.batchnavn(BATCHNAVN)
										.build()))
								.build()))
				.build();
	}

	public static OpprettJournalpostRequest createRequestOrg(JournalpostType journalpostType, String journalfoerendeEnhet, String sakId) {
		return createBaseRequestOrg(journalpostType, sakId)
				.journalfoerendeEnhet(journalfoerendeEnhet)
				.dokumenter(Arrays.asList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(Arrays.asList(DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.variantformat(VARIANTFORMAT_ARKIV)
												.fysiskDokument(FYSISK_DOKUMENT)
												.batchnavn(BATCHNAVN)
												.build(),
										DokumentVariant.builder()
												.filtype(FILTYPE_XML)
												.variantformat(VARIANTFORMAT_ORIGINAL)
												.filnavn(FILNAVN)
												.fysiskDokument(FYSISK_DOKUMENT_2)
												.batchnavn(BATCHNAVN)
												.build()))
								.build(),
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL2)
								.brevkode(BREVKODE2)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.rekkefoelge(2)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.batchnavn(BATCHNAVN)
										.build()))
								.build()))
				.build();
	}


	public static OpprettJournalpostRequest.OpprettJournalpostRequestBuilder createBaseRequest(JournalpostType journalpostType) {
		return OpprettJournalpostRequest.builder()
				.journalposttype(journalpostType)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(AVSENDER_ID_PERSON)
						.idType(AvsenderMottakerIdType.FNR)
						.navn(AVSENDER_NAVN)
						.land(AVSENDER_MOTTAKER_LAND)
						.build())
				.bruker(no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(BRUKER_ID_PERSON)
						.build())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(INNHOLD)
				.kanal(KANAL_NAVNO)
				.eksternReferanseId(KANALREFERANSE_ID)
				.datoDokument(DATO_DOKUMENT)
				.datoMottatt(DATO_MOTTATT)
				.tilleggsopplysninger(Collections.singletonList(Tilleggsopplysning.builder()
						.nokkel(TILLEGGSOPPLYSNING_NOKKEL)
						.verdi(TILLEGGSOPPLYSNING_VERDI)
						.build()))
				.sak(Sak.builder()
						.arkivsaksnummer(SAK_ID.toString())
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.build());
	}


	public static OpprettJournalpostRequest.OpprettJournalpostRequestBuilder createBaseRequest(JournalpostType journalpostType, String sakId) {
		return OpprettJournalpostRequest.builder()
				.journalposttype(journalpostType)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(AVSENDER_ID_PERSON)
						.idType(AvsenderMottakerIdType.FNR)
						.navn(AVSENDER_NAVN)
						.land(AVSENDER_MOTTAKER_LAND)
						.build())
				.bruker(no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(BRUKER_ID_PERSON)
						.build())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(INNHOLD)
				.kanal(KANAL_NAVNO)
				.eksternReferanseId(KANALREFERANSE_ID)
				.datoDokument(DATO_DOKUMENT)
				.datoMottatt(DATO_MOTTATT)
				.tilleggsopplysninger(Collections.singletonList(Tilleggsopplysning.builder()
						.nokkel(TILLEGGSOPPLYSNING_NOKKEL)
						.verdi(TILLEGGSOPPLYSNING_VERDI)
						.build()))
				.sak(Sak.builder()
						.fagsakId(sakId)
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(Fagsaksystem.PP01)
						.build());
	}

	public static OpprettJournalpostRequest.OpprettJournalpostRequestBuilder createBaseRequestOrg(JournalpostType journalpostType, String sakId) {
		return OpprettJournalpostRequest.builder()
				.journalposttype(journalpostType)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(AVSENDER_ID_ORGANISASJON)
						.idType(ORGNR)
						.land(AVSENDER_MOTTAKER_LAND)
						.build())
				.bruker(no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(BRUKER_ID_PERSON)
						.build())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(INNHOLD)
				.kanal(KANAL_NAVNO)
				.eksternReferanseId(KANALREFERANSE_ID)
				.datoDokument(DATO_DOKUMENT)
				.datoMottatt(DATO_MOTTATT)
				.tilleggsopplysninger(Collections.singletonList(Tilleggsopplysning.builder()
						.nokkel(TILLEGGSOPPLYSNING_NOKKEL)
						.verdi(TILLEGGSOPPLYSNING_VERDI)
						.build()))
				.sak(Sak.builder()
						.fagsakId(sakId)
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(Fagsaksystem.PP01)
						.build());
	}

	public static OpprettJournalpostRequest.OpprettJournalpostRequestBuilder createMinimalRequest(JournalpostType journalpostType) {
		return OpprettJournalpostRequest.builder()
				.journalposttype(journalpostType)
				.tema(FagomradeCode.FOR.name())
				.kanal(journalpostType == INNGAAENDE ? "NAV_NO" : null)
				.eksternReferanseId("eksternReferanseId")
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.rekkefoelge(1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(List.of(
										DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.fysiskDokument(FYSISK_DOKUMENT)
												.variantformat(VARIANTFORMAT_ARKIV)
												.build()))
								.build()));
	}

	public static OpprettJournalpostRequest.OpprettJournalpostRequestBuilder createMinimalRequestWithoutEksternReferanseId(JournalpostType journalpostType) {
		return OpprettJournalpostRequest.builder()
				.journalposttype(journalpostType)
				.tema(FagomradeCode.FOR.name())
				.kanal(journalpostType == INNGAAENDE ? "NAV_NO" : null)
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(List.of(
										DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.fysiskDokument(FYSISK_DOKUMENT)
												.variantformat(VARIANTFORMAT_ARKIV)
												.build()))
								.build()));
	}


	public static OpprettJournalpostRequest createMinimalRequestWithKanal(String kanal) {
		return OpprettJournalpostRequest.builder()
				.journalposttype(UTGAAENDE)
				.tema(FagomradeCode.FOR.name())
				.eksternReferanseId(KANALREFERANSE_ID)
				.kanal(kanal)
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(List.of(
										DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.fysiskDokument(FYSISK_DOKUMENT)
												.variantformat(VARIANTFORMAT_ARKIV)
												.build()))
								.build()))
				.build();
	}

	public static OpprettJournalpostRequest createMinimalRequestWithBrevkode(String brevkode) {
		return OpprettJournalpostRequest.builder()
				.journalposttype(UTGAAENDE)
				.tema(FagomradeCode.FOR.name())
				.eksternReferanseId(KANALREFERANSE_ID)
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(brevkode)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.build()))
				.build();
	}

	public static OpprettJournalpostRequest.OpprettJournalpostRequestBuilder createMinimalRequestWithAvsenderMottaker(JournalpostType journalpostType) {
		return createMinimalRequest(journalpostType).avsenderMottaker(AvsenderMottaker.builder()
				.id(AVSENDER_ID_PERSON)
				.idType(AvsenderMottakerIdType.FNR)
				.navn(AVSENDER_NAVN)
				.land(AVSENDER_MOTTAKER_LAND)
				.build());
	}

	public static OpprettJournalpostRequest createRequestAvsenderMottaker(JournalpostType journalpostType, AvsenderMottaker avsenderMottaker) {
		return OpprettJournalpostRequest.builder()
				.journalposttype(journalpostType)
				.avsenderMottaker(avsenderMottaker)
				.tema(TEMA_FOR)
				.tittel(INNHOLD)
				.build();
	}

	public static TilknyttVedleggRequest createTilknyttVedleggRequest() {
		return TilknyttVedleggRequest.builder()
				.tilknyttetAvNavn("Testus Testesen")
				.dokument(createDokumentVedleggList())
				.build();
	}

	public static TilknyttVedleggRequest createTilknyttVedleggRequest(String tilknyttetAvNavn, List<DokumentVedlegg> dokumentVedleggList) {
		return TilknyttVedleggRequest.builder()
				.tilknyttetAvNavn(tilknyttetAvNavn)
				.dokument(dokumentVedleggList)
				.build();
	}

	public static List<DokumentVedlegg> createDokumentVedleggList() {
		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		DokumentVedlegg dokumentVedlegg = DokumentVedlegg.builder()
				.dokumentInfoId(DOKUMENTINFO_ID1)
				.kildeJournalpostId(JOURNALPOST_ID)
				.build();
		dokumentVedleggList.add(dokumentVedlegg);
		return dokumentVedleggList;
	}

	public static List<DokumentVedlegg> createDokumentVedleggList(Long journalpostId, String dokumentInfoId) {
		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(createDokumentVedlegg(journalpostId, dokumentInfoId));
		return dokumentVedleggList;
	}

	public static DokumentVedlegg createDokumentVedlegg(Long journalpostId, String dokumentInfoId) {
		return createDokumentVedlegg(journalpostId, dokumentInfoId, null);
	}

	public static DokumentVedlegg createDokumentVedlegg(Long journalpostId, String dokumentInfoId, Integer rekkefoelge) {
		return DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.rekkefoelge(rekkefoelge)
				.build();
	}

	public static no.nav.dokarkiv.core.domain.entities.Sak createGenerellSak() {
		return no.nav.dokarkiv.core.domain.entities.Sak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_SYM)
				.applikasjon(FS22.name())
				.opprettetAv(CONSUMER_ID)
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
	}

	public static no.nav.dokarkiv.core.domain.entities.Sak createFagsak() {
		return no.nav.dokarkiv.core.domain.entities.Sak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_TIL)
				.applikasjon(AO01.name())
				.fagsakNr(FAGSAK_ID)
				.opprettetAv(CONSUMER_ID)
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
	}

	public static EregResponse createEregResponse(String organisasjonId, String organisasjonNavn) {
		var bruksperiode = new EregResponse.Navn.Bruksperiode(FORTID, FREMTID);
		var gyldighetsperiode = new EregResponse.Navn.Gyldighetsperiode(FORTID_DATO, FREMTID_DATO);

		return new EregResponse(organisasjonId, new EregResponse.Navn(organisasjonNavn, bruksperiode, gyldighetsperiode));
	}

	public static EregResponse createEregResponseWithBruksperiode(String organisasjonId, String organisasjonNavn, LocalDateTime bruksperiodeStart, LocalDateTime bruksperiodeSlutt) {
		var gyldighetsperiode = new EregResponse.Navn.Gyldighetsperiode(FORTID_DATO, FREMTID_DATO);
		var bruksperiode = new EregResponse.Navn.Bruksperiode(bruksperiodeStart, bruksperiodeSlutt);

		return new EregResponse(organisasjonId, new EregResponse.Navn(organisasjonNavn, bruksperiode, gyldighetsperiode));
	}
}
