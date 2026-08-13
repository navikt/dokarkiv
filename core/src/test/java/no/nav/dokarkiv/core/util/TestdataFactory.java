package no.nav.dokarkiv.core.util;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.Innsyn;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.SakStatusCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.PEN;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.SKJULES_BRUKERS_ONSKE;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.SKJULES_INNSKRENKET_PARTSINNSYN;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AAPEN;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.repository.DokumentFilSkjermetRepository.FIL_UUID_DUMMY_DOKUMENT_KASSERT;
import static no.nav.dokarkiv.core.repository.DokumentFilSkjermetRepository.FIL_UUID_DUMMY_DOKUMENT_SKJERMET;

public class TestdataFactory {

	// --- Constants ---

	public static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	public static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	private static final String ENDRET_AV_NAVN = "Endret av navn";
	private static final String AVSENDER_MOTTAKER_ID = "02016126007";
	public static final String AVSENDER_MOTTAKER_NAVN = "Jim Hopper";
	public static final String AVSENDER_MOTTAKER_LAND = "NO";
	public static final AvsenderMottakerIdTypeCode AVSENDER_MOTTAKER_ID_TYPE = AvsenderMottakerIdTypeCode.FNR;
	public static final String TITTEL = "FysiskSlettDokument";
	public static final String BREVKODE = "Brevkode";
	public static final String BRUKER_ID = "12321305432";
	private static final String KRYSSREFERANSE_ID = "123213";
	public static final String DOKUMENT_INFO_TITTEL = "TITTEL";
	public static final String DOKUMENT_TYPE_ID = "0000001";
	public static final String API_GSAK_ID = "1232131233";
	private static final String API_PSAK_ID = "90909090";
	public static final String FIL_NAVN = "navn";
	private static final String TILLEGGOPPLYSNINGER_KEY = "tillegg";
	private static final String TILLEGGOPPLYSNINGER_VAL = "tillegg_verdi";
	public static final String INNHOLD = "Innhold";
	public static final byte[] FIL = "Test dokument".getBytes();
	public static final byte[] FIL_DUMMY_KASSERT = "Test kassert dummy dokument dummy".getBytes();
	private static final byte[] FIL_DUMMY_SKJERMET = "Test skjermet dummy dokument dummy".getBytes();
	private static final Integer ANTALL_RETUR = 3;
	public static final String KANAL_REFERANSE_ID = "KANAL_REFERANSE_ID";
	public static final String AKTOER_ID = "111113333333";
	private static final LocalDateTime LESTDATO = LocalDateTime.now().minusDays(3);
	public static final String ADRESSELINJE1 = "adresselinje1";
	public static final String ADRESSELINJE2 = "adresselinje2";
	public static final String ADRESSELINJE3 = "adresselinje3";
	private static final String POSTNUMMER = "postnummer";
	private static final String POSTSTED = "poststed";
	private static final String LANDKODE_NO = "NO";
	public static final String SKANNET_INNHOLD_TITTEL = "Henvendelse fra lege";
	public static final String SKANNET_INNHOLD_TITTEL_2 = "Dokumentasjon";
	public static final String BEHANDLINGSTEMA = "ab0438";
	public static final String JOURNALFOERENDE_ENHET = "9999";
	public static final String JOURNALFOERT_AV_NAVN = "Bjarne Betjent";
	public static final SkjermingTypeCode SKJERMING_TYPE_CODE = SkjermingTypeCode.POL;
	public static final String GSAK_FAGSAKNR = "1234";
	public static final String GSAK_TEMA = "RPO";
	public static final String GSAK_APPLIKASJON = "AO01";
	private static final String GSAK_OPPRETTET_AV = "itest";
	public static final String TILLEGGOPPLYSNINGER_KEY_1 = "tillegg1";
	public static final String TILLEGGOPPLYSNINGER_VAL_1 = "tillegg_verdi1";
	public static final String TILLEGGOPPLYSNINGER_KEY_2 = "tillegg2";
	public static final String TILLEGGOPPLYSNINGER_VAL_2 = "tillegg_verdi2";
	public static final String TILLEGGOPPLYSNINGER_KEY_3 = "tillegg3";
	public static final String TILLEGGOPPLYSNINGER_VAL_3 = "tillegg_verdi3";
	public static final String TILLEGGOPPLYSNINGER_KEY_4 = "tillegg4";
	public static final String TILLEGGOPPLYSNINGER_VAL_4 = "tillegg_verdi4";
	private static final String FIL_UUID_ARKIV_HOVEDDOKUMENT = "filUuidHoveddokumentArkiv";
	private static final String FIL_UUID_PRODUKSJON_HOVEDDOKUMENT = "filUuidHoveddokumentProduksjon";
	private static final String FIL_UUID_ARKIV_VEDLEGG = "filUuidVedleggArkiv";
	private static final String FIL_UUID_PRODUKSJON_VEDLEGG = "filUuidVedleggProduksjon";
	public static final String GSAK_ORGNR = "812345678";
	public static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2023-08-11T12:01:01.001Z"), ZONEID_NORGE);

	// --- Journalpost ---

	public static Journalpost createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(long sakId) {
		return createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSaksrelasjon(createSaksrelasjon(sakId));
	}

	public static Journalpost createFullyPopulatedJournalpostWithHoveddokumentAndVedleggMedSak() {
		return createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSaksrelasjon(null);
	}

	public static Journalpost createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSaksrelasjon(Saksrelasjon saksrelasjon) {
		return createJournalpost(saksrelasjon, JournalpostTypeCode.U, FS);
	}

	public static Journalpost createJournalpost(long sakId, JournalpostTypeCode journalpostTypeCode, JournalStatusCode statusCode) {
		return createJournalpost(createSaksrelasjon(sakId), journalpostTypeCode, statusCode);
	}

	public static Journalpost createJournalpost(Saksrelasjon saksrelasjon, JournalpostTypeCode journalpostTypeCode, JournalStatusCode statusCode) {
		Journalpost journalpost = Journalpost.builder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottaker(AVSENDER_MOTTAKER_NAVN)
				.avsenderMottakerIdType(AVSENDER_MOTTAKER_ID_TYPE)
				.land(AVSENDER_MOTTAKER_LAND)
				.mottattDato(LocalDateTime.now(FIXED_CLOCK))
				.journalDato(LocalDateTime.now(FIXED_CLOCK))
				.sendtPrintDato(LocalDateTime.now(FIXED_CLOCK))
				.ekspedertDato(LocalDateTime.now(FIXED_CLOCK))
				.avsendtReturDato(LocalDateTime.now(FIXED_CLOCK))
				.dokumentDato(LocalDateTime.now(FIXED_CLOCK))
				.lestDato(LocalDateTime.now(FIXED_CLOCK))
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalstatus(statusCode)
				.journalposttype(journalpostTypeCode)
				.innhold(INNHOLD)
				.behandlingstema(BEHANDLINGSTEMA)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.antallRetur(ANTALL_RETUR)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.journalfortAvNavn(JOURNALFOERT_AV_NAVN)
				.innsyn(BRUK_STANDARDREGLER)
				.skjermingType(SKJERMING_TYPE_CODE)
				.build();

		journalpost.setChangeStamp(new ChangeStamp("itest", LocalDateTime.now(FIXED_CLOCK), null, null));
		journalpost.addBruker(createBruker());
		journalpost.addBruker(createBruker());
		journalpost.addKryssReferanse(createKryssreferanse());
		journalpost.setTilleggsopplysninger(createTilleggsopplysninger());
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

		if (saksrelasjon != null) {
			journalpost.setSaksrelasjon(saksrelasjon);
		}

		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost));
		journalpost.addJournalpostDokumentInfoRelasjon(createDokumentInfoVedleggRelasjon(journalpost));
		return journalpost;
	}

	public static Journalpost createFerdigstiltJournalpostWithHoveddokument() {
		Journalpost journalpost = createBaseJournalpost();
		journalpost.setKanalReferanseId(KANAL_REFERANSE_ID);
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setOriginalJournalpost(journalpost);
		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost, dokumentInfo));
		return journalpost;
	}

	public static Journalpost createUbehandletJournalpost(
			LocalDateTime dateTime,
			JournalpostTypeCode journalpostTypeCode,
			JournalStatusCode journalStatusCode
	) {
		return createUbehandletJournalpost(dateTime, journalpostTypeCode, journalStatusCode, PEN);
	}

	public static Journalpost createUbehandletJournalpost(
			LocalDateTime dateTime,
			JournalpostTypeCode journalpostTypeCode,
			JournalStatusCode journalStatusCode,
			FagomradeCode fagomradeCode
	) {
		Journalpost journalpost = Journalpost.builder()
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.journalposttype(journalpostTypeCode)
				.journalstatus(journalStatusCode)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.journalDato(LocalDateTime.now())
				.kanalReferanseId(KANAL_REFERANSE_ID + UUID.randomUUID())
				.endretAvNavn(ENDRET_AV_NAVN)
				.fagomrade(fagomradeCode)
				.mottakskanal(NAV_NO)
				.behandlingstema(BEHANDLINGSTEMA)
				.tilleggsopplysninger(Map.of("key", "value"))
				.build();

		journalpost.addBruker(createBruker());
		journalpost.setSaksrelasjon(createSaksrelasjon(1L, journalpost));
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpost.setChangeStamp(new ChangeStamp("createdBy", dateTime, "String updatedBy", dateTime));

		return journalpost;
	}

	public static Journalpost createJournalpostWithSplittetHoveddokument(Journalpost journalpostOriginal) {
		Journalpost journalpost = createBaseJournalpost();
		journalpost.setInnsyn(SKJULES_BRUKERS_ONSKE);
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setOriginalJournalpost(journalpostOriginal);
		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpostOriginal, dokumentInfo));
		return journalpost;
	}

	public static Journalpost createJournalpostWithGjenbruktHoveddokument(DokumentInfo dokumentInfoGjenbrukt) {
		Journalpost journalpost = createBaseJournalpost();
		journalpost.setInnsyn(SKJULES_INNSKRENKET_PARTSINNSYN);
		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost, dokumentInfoGjenbrukt));
		return journalpost;
	}

	public static Journalpost createReservertPensjonJournalpost(String arkivFilUuid, String produksjonFilUuid) {
		Journalpost journalpost = Journalpost.builder()
				.journalposttype(JournalpostTypeCode.U)
				.journalstatus(JournalStatusCode.D)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.innhold(INNHOLD)
				.avsenderMottaker(AVSENDER_MOTTAKER_NAVN)
				.dokumentDato(LocalDateTime.now())
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.endretAvNavn(ENDRET_AV_NAVN)
				.fagomrade(PEN)
				.build();

		journalpost.addBruker(createBruker());
		journalpost.setSaksrelasjon(createPsakSaksrelasjon());
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost, createUnderRedigeringPensjonDokumentInfo(arkivFilUuid, produksjonFilUuid)));
		return journalpost;
	}

	public static Journalpost createBaseJournalpost() {
		Journalpost journalpost = Journalpost.builder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottaker(AVSENDER_MOTTAKER_NAVN)
				.avsenderMottakerIdType(AVSENDER_MOTTAKER_ID_TYPE)
				.land(AVSENDER_MOTTAKER_LAND)
				.dokumentDato(LocalDateTime.now())
				.journalDato(LocalDateTime.now())
				.avsendtReturDato(LocalDateTime.now())
				.sendtPrintDato(LocalDateTime.now())
				.ekspedertDato(LocalDateTime.now())
				.lestDato(LESTDATO)
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalstatus(FS)
				.journalposttype(JournalpostTypeCode.U)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.journalfortAvNavn(JOURNALFOERT_AV_NAVN)
				.innhold(INNHOLD)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.mottakskanal(NAV_NO)
				.antallRetur(ANTALL_RETUR)
				.innsyn(BRUK_STANDARDREGLER)
				.build();
		journalpost.addBruker(createBruker());
		journalpost.addKryssReferanse(createKryssreferanse());
		journalpost.setSaksrelasjon(createSaksrelasjon(journalpost));
		journalpost.setTilleggsopplysninger(Map.of(TILLEGGOPPLYSNINGER_KEY, TILLEGGOPPLYSNINGER_VAL));
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return journalpost;
	}

	// --- JournalpostDokumentInfoRelasjon ---

	public static JournalpostDokumentInfoRelasjon createHoveddokumentRelasjon(Journalpost journalpost) {
		DokumentInfo dokumentInfo = createDokumentInfo(FIL_UUID_ARKIV_HOVEDDOKUMENT, FIL_UUID_PRODUKSJON_HOVEDDOKUMENT);
		dokumentInfo.setOriginalJournalpost(journalpost);
		return createHoveddokumentRelasjon(journalpost, dokumentInfo);
	}

	public static JournalpostDokumentInfoRelasjon createHoveddokumentRelasjon(Journalpost journalpost, DokumentInfo dokumentInfo) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		return journalpostDokumentInfoRelasjon;
	}

	public static JournalpostDokumentInfoRelasjon createDokumentInfoVedleggRelasjon(Journalpost journalpost) {
		return createDokumentInfoVedleggRelasjon(journalpost, null);
	}

	public static JournalpostDokumentInfoRelasjon createDokumentInfoVedleggRelasjon(Journalpost journalpost, Integer rekkefoelge) {
		DokumentInfo dokumentInfo = createDokumentInfo(FIL_UUID_ARKIV_VEDLEGG, FIL_UUID_PRODUKSJON_VEDLEGG);
		dokumentInfo.setOriginalJournalpost(journalpost);
		return createVedleggRelasjon(journalpost, dokumentInfo, rekkefoelge);
	}

	public static JournalpostDokumentInfoRelasjon createVedleggRelasjon(Journalpost journalpost, DokumentInfo dokumentInfo) {
		return createVedleggRelasjon(journalpost, dokumentInfo, null);
	}

	public static JournalpostDokumentInfoRelasjon createVedleggRelasjon(Journalpost journalpost, DokumentInfo dokumentInfo, Integer rekkefoelge) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.rekkefoelge(rekkefoelge)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		journalpost.addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);
		return journalpostDokumentInfoRelasjon;
	}

	public static DokumentInfo getDokumentInfoFromJpDokInfoRelasjoner(Journalpost journalpost, int nr) {
		return journalpost.getJournalpostDokumentInfoRelasjoner().stream()
				.skip(nr)
				.findFirst()
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
				.orElse(null);
	}

	public static void setSkjermingVedlegg(Journalpost actualJournalpost) {
		actualJournalpost.getJournalpostDokumentInfoRelasjoner().stream()
				.filter(JournalpostDokumentInfoRelasjon::isVedlegg)
				.forEach(vedlegg -> vedlegg.getDokumentInfo().setSkjermingType(POL));
	}

	// --- Saksrelasjon og Sak ---

	public static Saksrelasjon createSaksrelasjon(long sakId, Journalpost... journalpost) {
		Saksrelasjon saksrelasjon = Saksrelasjon.builder()
				.fagsystem(FagsystemCode.FS22)
				.sakId(sakId)
				.feilregistrert(false)
				.build();

		if (journalpost.length > 0) {
			saksrelasjon.setJournalpost(journalpost[0]);
		}
		saksrelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return saksrelasjon;
	}

	public static Saksrelasjon createSaksrelasjon(Journalpost journalpost) {
		return createSaksrelasjon(parseLong(API_GSAK_ID), journalpost);
	}

	public static Saksrelasjon createSaksrelasjon(Journalpost journalpost, Long sakId) {
		return createSaksrelasjon(sakId, journalpost);
	}

	public static Saksrelasjon createPsakSaksrelasjon() {
		Saksrelasjon saksrelasjon = Saksrelasjon.builder()
				.fagsystem(FagsystemCode.PEN)
				.sakId(parseLong(API_PSAK_ID))
				.build();
		saksrelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return saksrelasjon;
	}

	public static Sak createSakForAktoerId(String fagomrade, String aktoerId, String fagsystem, String fagsakNr) {
		return createBaseSak(fagomrade, fagsystem, fagsakNr, AAPEN)
				.aktoerId(aktoerId)
				.build();
	}

	public static Sak createSakForOrgNr(String fagomrade, String orgnr, String fagsystem, String fagsakNr) {
		return createBaseSak(fagomrade, fagsystem, fagsakNr, AAPEN)
				.orgnr(orgnr)
				.build();
	}

	public static Sak.SakBuilder createBaseSak(String fagomrade, String fagsystem, String fagsakNr, SakStatusCode sakStatusCode) {
		return Sak.builder()
				.tema(fagomrade)
				.fagsakNr(fagsakNr)
				.applikasjon(fagsystem)
				.opprettetAv("Donald Duck")
				.endretAv("Donald Duck")
				.sakStatus(sakStatusCode)
				.opprettetTidspunkt(LocalDateTime.of(2023, 8, 20, 15, 15));
	}

	public static Sak createGsak() {
		return Sak.builder()
				.aktoerId(AKTOER_ID)
				.fagsakNr(GSAK_FAGSAKNR)
				.orgnr(GSAK_ORGNR)
				.tema(GSAK_TEMA)
				.sakStatus(SakStatusCode.AAPEN)
				.applikasjon(GSAK_APPLIKASJON)
				.opprettetAv(GSAK_OPPRETTET_AV)
				.opprettetTidspunkt(LocalDate.now(FIXED_CLOCK).atStartOfDay())
				.build();
	}

	// --- Bruker og Kryssreferanse ---

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

	// --- DokumentInfo ---

	public static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = baseDokumentInfoBuilder()
				.dokumentFerdigDato(LocalDateTime.now())
				.build();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.ARKIV));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.PRODUKSJON));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold(SKANNET_INNHOLD_TITTEL));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold(SKANNET_INNHOLD_TITTEL_2));
		dokumentInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		dokumentInfo.setTilleggsopplysninger(Map.of(TILLEGGOPPLYSNINGER_KEY, TILLEGGOPPLYSNINGER_VAL));
		return dokumentInfo;
	}

	public static DokumentInfo createDokumentInfo(String filUuidArkiv, String filUuidProduksjon) {
		DokumentInfo dokumentInfo = baseDokumentInfoBuilder()
				.dokumentFerdigDato(LocalDateTime.now(FIXED_CLOCK))
				.build();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.ARKIV, FilTypeCode.PDF, filUuidArkiv));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.PRODUKSJON, FilTypeCode.JSON, filUuidProduksjon));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold(SKANNET_INNHOLD_TITTEL));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold(SKANNET_INNHOLD_TITTEL_2));
		dokumentInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		dokumentInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		return dokumentInfo;
	}

	public static DokumentInfo createDokumentInfoWithMoreData() {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(DOKUMENT_INFO_TITTEL)
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.build();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.ARKIV));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold(SKANNET_INNHOLD_TITTEL));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold(SKANNET_INNHOLD_TITTEL_2));
		dokumentInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		dokumentInfo.setTilleggsopplysninger(Map.of(TILLEGGOPPLYSNINGER_KEY, TILLEGGOPPLYSNINGER_VAL));
		dokumentInfo.setKategori(DokumentKategoriCode.B);
		dokumentInfo.setKassert(false);
		dokumentInfo.setSensitivt(true);
		return dokumentInfo;
	}

	public static DokumentInfo createUnderRedigeringPensjonDokumentInfo(String arkivFilUuid, String produksjonFilUuid) {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
				.tittel(DOKUMENT_INFO_TITTEL)
				.brevkode(BREVKODE)
				.kassert(false)
				.kategori(DokumentKategoriCode.B)
				.build();
		if (produksjonFilUuid != null) {
			FilDetaljer produksjonFilDetaljer = createFildetaljerOgFil(dokumentInfo, FilTypeCode.RTF, VariantFormatCode.PRODUKSJON, produksjonFilUuid, null);
			dokumentInfo.addFilDetaljer(produksjonFilDetaljer);
		}
		if (arkivFilUuid != null) {
			FilDetaljer arkivFilDetaljer = createFildetaljerOgFil(dokumentInfo, FilTypeCode.PDF, VariantFormatCode.ARKIV, arkivFilUuid, null);
			dokumentInfo.addFilDetaljer(arkivFilDetaljer);
		}
		dokumentInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return dokumentInfo;
	}

	private static DokumentInfo.DokumentInfoBuilder baseDokumentInfoBuilder() {
		return DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(DOKUMENT_INFO_TITTEL)
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.brevkode(BREVKODE)
				.kassert(false)
				.kategori(DokumentKategoriCode.ES)
				.sensitivt(true);
	}

	// --- SkannetInnhold ---

	public static SkannetInnhold createSkannetInnhold(String tittel) {
		SkannetInnhold skannetInnhold = SkannetInnhold.builder()
				.vedleggInnhold(tittel)
				.dokumenttypeid(DOKUMENT_TYPE_ID)
				.build();
		skannetInnhold.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return skannetInnhold;
	}

	// --- FilDetaljer og DokumentFil ---

	public static FilDetaljer createFildetaljerOgFil(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode) {
		return createFildetaljerOgFil(dokumentInfo, FilTypeCode.PDF, variantFormatCode, FilDetaljer.generateUuid(), FIL_NAVN);
	}

	public static FilDetaljer createFildetaljerOgFil(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, String uuid) {
		return createFildetaljerOgFil(dokumentInfo, FilTypeCode.PDF, variantFormatCode, uuid, FIL_NAVN);
	}

	public static FilDetaljer createFildetaljerOgFilMedFilnavn(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, String filnavn) {
		return createFildetaljerOgFil(dokumentInfo, FilTypeCode.PDF, variantFormatCode, FilDetaljer.generateUuid(), filnavn);
	}

	public static FilDetaljer createFildetaljerOgFil(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, FilTypeCode filTypeCode, String filUuid) {
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.dokumentInfo(dokumentInfo)
				.fileContent(FIL)
				.filnavn(FIL_NAVN)
				.filtype(filTypeCode)
				.filUuid(filUuid)
				.filstorrelse(String.valueOf(FIL.length))
				.variantFormat(variantFormatCode)
				.skjermingType(POL)
				.build();
		filDetaljer.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return filDetaljer;
	}

	public static FilDetaljer createFildetaljerOgFil(DokumentInfo dokumentInfo, FilTypeCode filTypeCode, VariantFormatCode variantFormatCode, String filUuid, String filnavn) {
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.dokumentInfo(dokumentInfo)
				.fileContent(FIL)
				.filnavn(filnavn)
				.filtype(filTypeCode)
				.filUuid(filUuid)
				.filstorrelse(String.valueOf(FIL.length))
				.variantFormat(variantFormatCode)
				.build();
		filDetaljer.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return filDetaljer;
	}

	public static DokumentFil createDummyDokumentKassert() {
		DokumentFil dokumentFil = new DokumentFil();
		dokumentFil.setFil(FIL_DUMMY_KASSERT);
		dokumentFil.setFilUuid(FIL_UUID_DUMMY_DOKUMENT_KASSERT);
		dokumentFil.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return dokumentFil;
	}

	public static DokumentFil createDummyDokumentSkjermet() {
		DokumentFil dokumentFil = new DokumentFil();
		dokumentFil.setFil(FIL_DUMMY_SKJERMET);
		dokumentFil.setFilUuid(FIL_UUID_DUMMY_DOKUMENT_SKJERMET);
		dokumentFil.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return dokumentFil;
	}

	// --- UtsendingsInfo ---

	public static UtsendingsInfo createFysiskpostUtsendingsInfo(Journalpost journalpost) {
		var fysiskpostadresse = new UtsendingsInfo.FysiskPostadresse(ADRESSELINJE1, ADRESSELINJE2, ADRESSELINJE3, POSTNUMMER, POSTSTED, LANDKODE_NO);
		return new UtsendingsInfo(journalpost, fysiskpostadresse);
	}

	public static UtsendingsInfo createNavNoUtsendingsInfo(Journalpost journalpost) {
		var utsendingsInfoPart = new UtsendingsInfo.NavNoVarsling("navno-identifikator-for-mottaker", "varslingstekst");
		return new UtsendingsInfo(journalpost, utsendingsInfoPart, digitalEpostVarsler(), digitalSmsVarsler());
	}

	public static UtsendingsInfo createDigitalPostUtsendingsInfo(Journalpost journalpost) {
		UtsendingsInfo.DigitalPostadresse digitalPostadresse = new UtsendingsInfo.DigitalPostadresse("bjarne.betjent#12AB", "12345678");
		return new UtsendingsInfo(journalpost, digitalPostadresse, digitalEpostVarsler(), digitalSmsVarsler());
	}

	private static UtsendingsInfo.EpostVarsler digitalEpostVarsler() {
		return new UtsendingsInfo.EpostVarsler(List.of(new UtsendingsInfo.EpostVarsel("tittel", "tekst", "homer@epos.gr", "2023-02-27T12:30:00.000")));
	}

	private static UtsendingsInfo.SmsVarsler digitalSmsVarsler() {
		return new UtsendingsInfo.SmsVarsler(List.of(new UtsendingsInfo.SmsVarsel("tekst", "+4700000000", "2023-02-27T12:30:00.000")));
	}

	// --- Misc ---

	static Map<String, String> createTilleggsopplysninger() {
		return Map.of(
				TILLEGGOPPLYSNINGER_KEY_1, TILLEGGOPPLYSNINGER_VAL_1,
				TILLEGGOPPLYSNINGER_KEY_2, TILLEGGOPPLYSNINGER_VAL_2,
				TILLEGGOPPLYSNINGER_KEY_3, TILLEGGOPPLYSNINGER_VAL_3,
				TILLEGGOPPLYSNINGER_KEY_4, TILLEGGOPPLYSNINGER_VAL_4
		);
	}

	public static Stream<Innsyn> generateInnsynWithDescription() {
		return Stream.of(InnsynCode.values())
				.map(code -> Innsyn.builder()
						.kode(code.name())
						.beskrivelse("beskrivelse av " + code.name())
						.build());
	}
}
