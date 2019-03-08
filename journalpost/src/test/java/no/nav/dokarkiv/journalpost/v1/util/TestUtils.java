package no.nav.dokarkiv.journalpost.v1.util;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
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
import no.nav.dokarkiv.journalpost.v1.api.Arkivsak;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.PutOppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class TestUtils {

	public static final long JOURNALPOST_ID = 1234L;
	public static final String AVSENDER_NAVN = "avsenderNavn";
	public static final String AVSENDER_NAVN_ORGANISASJON = "avsenderNavn_org";
	public static final String AVSENDER_ID_PERSON = "***gammelt_fnr***";
	public static final String AVSENDER_ID_ORGANISASJON = "123456789";
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
	public static final String BEHANDLINGSTEMA = "ab0001";
	public static final String AVSENDER_MOTTAKER_LAND = "Legoland";

	public static Journalpost createJournalpost() {
		Journalpost journalpost = Journalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.journalstatus(JournalStatusCode.J)
				.avsenderMottakerId(AVSENDER_ID_PERSON)
				.avsenderMottaker(AVSENDER_NAVN)
				.journalposttype(JournalpostTypeCode.I)
				.fagomrade(FagomradeCode.FS22)
				.innhold(INNHOLD)
				.kanalReferanseId(KANALREFERANSE_ID)
				.mottakskanal(MottaksKanalCode.ALTINN)
				.mottattDato(Date.from(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC)))
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.saksrelasjon(Saksrelasjon.builder()
						.sakId(SAK_ID)
						.fagsystem(FagsystemCode.FS22)
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
				.fagomrade(FagomradeCode.FS22)
				.innhold(INNHOLD)
				.kanalReferanseId(KANALREFERANSE_ID)
				.mottakskanal(MottaksKanalCode.ALTINN)
				.mottattDato(Date.from(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC)))
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.saksrelasjon(Saksrelasjon.builder()
						.sakId(SAK_ID)
						.fagsystem(FagsystemCode.FS22)
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

	public static PutOppdaterJournalpostRequest createPutOppdaterJournalpostRequest() {
		return PutOppdaterJournalpostRequest.builder()
				.avsenderMottaker(createAvsenderMottakerPerson())
				.bruker(createBrukerPerson())
				.arkivsak(createArkivSak())
				.tema(TEMA_FOR)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(DOKUMENT_TITTEL1)
				.avsenderMottakerLand(AVSENDER_MOTTAKER_LAND)
				.tilleggsopplysninger(createTilleggsopplysninger())
				.dokumentInfoList(createDokumentInfos())
				.build();
	}

	private static AvsenderMottaker createAvsenderMottakerPerson() {
		return AvsenderMottaker.builder()
				.avsenderMottakerNavn(AVSENDER_NAVN)
				.identifikator(AVSENDER_ID_PERSON)
				.build();
	}

	private static AvsenderMottaker createAvsenderMottakerOrganisasjon() {
		return AvsenderMottaker.builder()
				.avsenderMottakerNavn(AVSENDER_NAVN_ORGANISASJON)
				.identifikator(AVSENDER_ID_ORGANISASJON)
				.build();
	}

	private static no.nav.dokarkiv.journalpost.v1.api.Bruker createBrukerPerson() {
		return no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
				.brukerIdType(BrukerIdType.FNR)
				.identifikator(BRUKER_ID_PERSON)
				.build();
	}

	private static no.nav.dokarkiv.journalpost.v1.api.Bruker createBrukerOrganisasjon() {
		return no.nav.dokarkiv.journalpost.v1.api.Bruker.builder()
				.brukerIdType(BrukerIdType.ORGNR)
				.identifikator(BRUKER_ID_ORGANISASJON)
				.build();
	}

	private static Arkivsak createArkivSak() {
		return Arkivsak.builder()
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
}
