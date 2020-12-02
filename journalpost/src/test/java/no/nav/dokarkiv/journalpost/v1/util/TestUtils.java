package no.nav.dokarkiv.journalpost.v1.util;

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
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.AKTOER_ID;
import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.AO01;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
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

	public static final Date MOTTAT_DATO = Date.from(LocalDateTime.of(2017, 2, 3, 10, 37, 30).toInstant(ZoneOffset.UTC));
	public static final String BRUKER_ID_PERSON = "10987654321";
	public static final String BRUKER_ID_ORGANISASJON = "987654321";
	public static final String SAK_ID = "12345";
	public static final String FAGSAK_ID = "fagsakId";
	public static final String ARKIVSAKSNUMMER = "1234567890";
	public static final String INNHOLD = "innhold";
	public static final String KANALREFERANSE_ID = "kanalreferansId";
	public static final Date DATO_MOTTATT = Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
	public static final Date DATO_MOTTATT_1 = Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
	public static final String JOURNALFOERENDE_ENHET = "4000";
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
	public static final String TEMA_PEN = "PEN";
	public static final String TEMA_UFO = "UFO";
	public static final String TEMA_TIL = "TIL";
	public static final String TEMA_SYM = "SYM";
	public static final String BEHANDLINGSTEMA = "ab9999";
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
	public static final String BATCHNAVN = "batchnavn";

	public static final String CONSUMER_ID = "consumerId";

	public static Journalpost createJournalpost() {
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
				.mottattDato(Date.from(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC)))
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

	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithDatoRetur(Date date) {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.datoRetur(date)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumenter(createDokumentInfos())
				.build();
	}


	public static OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithDatoMottat(Date date) {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.bruker(createBrukerPerson())
				.sak(createSak())
				.tema(TEMA_FOR)
				.datoMottatt(date)
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
				.avsenderMottaker(createAvsenderMottakerPersonWithoutId())
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
		return AvsenderMottaker.builder()
				.navn(AVSENDER_NAVN)
				.id(AVSENDER_ID_PERSON)
				.land(AVSENDER_MOTTAKER_LAND)
				.build();
	}

	public static AvsenderMottaker createAvsenderMottakerPersonWithoutId() {
		return AvsenderMottaker.builder()
				.idType(AvsenderMottakerIdType.FNR)
				.id(" ")
				.navn(AVSENDER_NAVN)
				.land(AVSENDER_MOTTAKER_LAND)
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

	public static AvsenderMottaker createAvsenderMottakerUtenIdType() {
		return AvsenderMottaker.builder()
				.navn(AVSENDER_NAVN_UTLORGANISASJON)
				.id(AVSENDER_ID_UTLORGANISASJON)
				.idType(null)
				.land(AVSENDER_MOTTAKER_LAND)
				.build();
	}


	public static no.nav.dokarkiv.journalpost.v1.api.Bruker createBrukerPerson() {
		return no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
				.idType(BrukerIdType.FNR)
				.id(BRUKER_ID_PERSON)
				.build();
	}

	public static no.nav.dokarkiv.journalpost.v1.api.Bruker createBrukerOrganisasjon() {
		return no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
				.idType(BrukerIdType.ORGNR)
				.id(BRUKER_ID_ORGANISASJON)
				.build();
	}

	public static Sak createSak() {
		return Sak.builder()
				.arkivsaksnummer(SAK_ID)
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

	public static OpprettJournalpostRequest createRequest(JournalpostType journalpostType) {
		return createRequest(journalpostType, null);
	}

	public static OpprettJournalpostRequest createRequest(JournalpostType journalpostType, String journalfoerendeEnhet) {
		return createBaseRequest(journalpostType)
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
				.journalpostType(journalpostType)
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
				.datoMottatt(DATO_MOTTATT)
				.tilleggsopplysninger(Collections.singletonList(Tilleggsopplysning.builder()
						.nokkel(TILLEGGSOPPLYSNING_NOKKEL)
						.verdi(TILLEGGSOPPLYSNING_VERDI)
						.build()))
				.sak(Sak.builder()
						.arkivsaksnummer(SAK_ID)
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.build());
	}

	public static OpprettJournalpostRequest.OpprettJournalpostRequestBuilder createMinimalRequest(JournalpostType journalpostType) {
		return OpprettJournalpostRequest.builder()
				.journalpostType(journalpostType)
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.build()));
	}

	public static OpprettJournalpostRequest createRequestAvsenderMottaker(JournalpostType journalpostType, AvsenderMottaker avsenderMottaker) {
		return OpprettJournalpostRequest.builder()
				.journalpostType(journalpostType)
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

	public static List<DokumentVedlegg> createDokumentVedleggList(Long journalpostId, String dokumentInfo) {
		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(createDokumentVedlegg(journalpostId, dokumentInfo).build());
		return dokumentVedleggList;
	}

	private static DokumentVedlegg.DokumentVedleggBuilder createDokumentVedlegg(Long journalpostId, String dokumentId) {
		return DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostId)
				.dokumentInfoId(dokumentId);
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
}
