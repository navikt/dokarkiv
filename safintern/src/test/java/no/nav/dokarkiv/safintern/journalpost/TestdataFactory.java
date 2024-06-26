package no.nav.dokarkiv.safintern.journalpost;


import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;

public class TestdataFactory {

	static final String BRUK_STANDARDREGLER_INNSYNSBESKRIVELSE = "Standardreglene avgjør om dokumentet vises";
	static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	static final String AVSENDER_MOTTAKER_ID = "02016126007";
	static final String AVSENDER_MOTTAKER_NAVN = "Jim Hopper";
	static final String AVSENDER_MOTTAKER_LAND = "NO";
	static final AvsenderMottakerIdTypeCode AVSENDER_MOTTAKER_ID_TYPE = AvsenderMottakerIdTypeCode.FNR;
	static final String BREVKODE = "Brevkode";
	static final String BRUKER_ID = "123213";
	static final String KRYSSREFERANSE_ID = "123213";
	static final String DOKUMENT_INFO_TITTEL = "TITTEL";
	static final String DOKUMENT_TYPE_ID = "0000001";
	static final String FIL_NAVN = "navn";
	static final String TILLEGGOPPLYSNINGER_KEY_1 = "tillegg1";
	static final String TILLEGGOPPLYSNINGER_VAL_1 = "tillegg_verdi1";
	static final String TILLEGGOPPLYSNINGER_KEY_2 = "tillegg2";
	static final String TILLEGGOPPLYSNINGER_VAL_2 = "tillegg_verdi2";
	static final String TILLEGGOPPLYSNINGER_KEY_3 = "tillegg3";
	static final String TILLEGGOPPLYSNINGER_VAL_3 = "tillegg_verdi3";
	static final String TILLEGGOPPLYSNINGER_KEY_4 = "tillegg4";
	static final String TILLEGGOPPLYSNINGER_VAL_4 = "tillegg_verdi4";
	static final String INNHOLD = "Innhold";
	static final byte[] FIL = "Test dokument".getBytes();
	static final Integer ANTALL_RETUR = 3;
	static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";
	static final String AKTOER_ID = "111113333333";
	static final String ADRESSELINJE1 = "adresselinje1";
	static final String ADRESSELINJE2 = "adresselinje2";
	static final String ADRESSELINJE3 = "adresselinje3";
	static final String POSTNUMMER = "postnummer";
	static final String POSTSTED = "poststed";
	static final String LANDKODE_NO = "NO";
	static final String SKANNET_INNHOLD_TITTEL = "Henvendelse fra lege";
	static final String BEHANDLINGSTEMA = "ab0438";
	static final String BEHANDLINGSTEMA_DEKODE = "Lønnskompensasjon";
	static final String JOURNALFOERENDE_ENHET = "9999";
	static final String JOURNALFOERT_AV_NAVN = "Bjarne Betjent";
	static final SkjermingTypeCode SKJERMING_TYPE_CODE = POL;
	static final String GSAK_FAGSAKNR = "1234";
	static final String GSAK_TEMA = "RPO";
	static final String GSAK_APPLIKASJON = "AO01";
	static final String GSAK_OPPRETTET_AV = "itest";
	static final String FIL_UUID_ARKIV_HOVEDDOKUMENT = "filUuidHoveddokumentArkiv";
	static final String FIL_UUID_PRODUKSJON_HOVEDDOKUMENT = "filUuidHoveddokumentProduksjon";
	static final String FIL_UUID_ARKIV_VEDLEGG = "filUuidVedleggArkiv";
	static final String FIL_UUID_PRODUKSJON_VEDLEGG = "filUuidVedleggProduksjon";
	static final String GSAK_ORGNR = "812345678";
	private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2023-08-11T12:01:01.001Z"), ZoneId.of("Europe/Oslo"));

	static Journalpost createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(Long sakId) {
		Journalpost journalpost = Journalpost.builder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottaker(AVSENDER_MOTTAKER_NAVN)
				.avsenderMottakerIdType(AVSENDER_MOTTAKER_ID_TYPE)
				.land(AVSENDER_MOTTAKER_LAND)
				.mottattDato(Date.from(Instant.now(FIXED_CLOCK)))
				.journalDato(Date.from(Instant.now(FIXED_CLOCK)))
				.sendtPrintDato(Date.from(Instant.now(FIXED_CLOCK)))
				.ekspedertDato(Date.from(Instant.now(FIXED_CLOCK)))
				.avsendtReturDato(Date.from(Instant.now(FIXED_CLOCK)))
				.dokumentDato(Date.from(Instant.now(FIXED_CLOCK)))
				.lestDato(Date.from(Instant.now(FIXED_CLOCK)))
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalstatus(JournalStatusCode.FS)
				.journalposttype(JournalpostTypeCode.U)
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

		journalpost.addBruker(createBruker());
		journalpost.addBruker(createBruker());
		journalpost.addKryssReferanse(createKryssreferanse());
		journalpost.setTilleggsopplysninger(createTilleggsopplysninger());
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

		if (sakId != null) {
			journalpost.setSaksrelasjon(createSaksrelasjon(journalpost, sakId));
		}

		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost));
		journalpost.addJournalpostDokumentInfoRelasjon(createDokumentInfoVedleggRelasjon(journalpost));
		return journalpost;
	}


	static Map<String, String> createTilleggsopplysninger() {
		Map<String, String> tilleggsopplysninger = new HashMap<>();
		tilleggsopplysninger.put(TILLEGGOPPLYSNINGER_KEY_1, TILLEGGOPPLYSNINGER_VAL_1);
		tilleggsopplysninger.put(TILLEGGOPPLYSNINGER_KEY_2, TILLEGGOPPLYSNINGER_VAL_2);
		tilleggsopplysninger.put(TILLEGGOPPLYSNINGER_KEY_3, TILLEGGOPPLYSNINGER_VAL_3);
		tilleggsopplysninger.put(TILLEGGOPPLYSNINGER_KEY_4, TILLEGGOPPLYSNINGER_VAL_4);
		return tilleggsopplysninger;
	}


	static JournalpostDokumentInfoRelasjon createHoveddokumentRelasjon(Journalpost journalpost) {
		DokumentInfo dokumentInfo = createDokumentInfo(FIL_UUID_ARKIV_HOVEDDOKUMENT, FIL_UUID_PRODUKSJON_HOVEDDOKUMENT);
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

	static JournalpostDokumentInfoRelasjon createHoveddokumentRelasjonGjenbruktDokumentInfo(Journalpost journalpost, DokumentInfo dokumentInfo) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		return journalpostDokumentInfoRelasjon;
	}

	static JournalpostDokumentInfoRelasjon createDokumentInfoVedleggRelasjon(Journalpost journalpost) {
		DokumentInfo dokumentInfo = createDokumentInfo(FIL_UUID_ARKIV_VEDLEGG, FIL_UUID_PRODUKSJON_VEDLEGG);
		dokumentInfo.setOriginalJournalpost(journalpost);

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(VEDLEGG)
				.skjermingType(POL)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		journalpost.addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);
		return journalpostDokumentInfoRelasjon;
	}

	static no.nav.dokarkiv.core.domain.entities.Saksrelasjon createSaksrelasjon(Journalpost journalpost, Long sakId) {
		no.nav.dokarkiv.core.domain.entities.Saksrelasjon saksrelasjon = no.nav.dokarkiv.core.domain.entities.Saksrelasjon.builder()
				.fagsystem(FagsystemCode.FS22)
				.sakId(sakId)
				.journalpost(journalpost)
				.feilregistrert(false)
				.build();
		saksrelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return saksrelasjon;
	}

	static no.nav.dokarkiv.core.domain.entities.Bruker createBruker() {
		no.nav.dokarkiv.core.domain.entities.Bruker bruker = Bruker.builder()
				.brukerType(BrukerTypeCode.PERSON)
				.brukerId(BRUKER_ID)
				.build();
		bruker.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return bruker;
	}

	static Kryssreferanse createKryssreferanse() {
		Kryssreferanse kryssreferanse = Kryssreferanse.builder()
				.referanseType(ReferanseTypeCode.SPOERSMAAL)
				.referanseId(KRYSSREFERANSE_ID)
				.referanseNr(1L)
				.build();
		kryssreferanse.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return kryssreferanse;
	}


	static DokumentInfo createDokumentInfo(String filUuidArkiv, String filUuidProduksjon) {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(DOKUMENT_INFO_TITTEL)
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.dokumentFerdigDato(Date.from(Instant.now(FIXED_CLOCK)))
				.brevkode(BREVKODE)
				.kassert(false)
				.kategori(DokumentKategoriCode.ES)
				.sensitivt(true)
				.build();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.ARKIV, FilTypeCode.PDF, filUuidArkiv));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.PRODUKSJON, FilTypeCode.JSON, filUuidProduksjon));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold());
		dokumentInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		dokumentInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		return dokumentInfo;
	}

	static SkannetInnhold createSkannetInnhold() {
		SkannetInnhold skannetInnhold = SkannetInnhold.builder()
				.vedleggInnhold(SKANNET_INNHOLD_TITTEL)
				.dokumenttypeid(DOKUMENT_TYPE_ID)
				.build();
		skannetInnhold.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return skannetInnhold;
	}


	static FilDetaljer createFildetaljerOgFil(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, FilTypeCode filTypeCode, String filUuid) {
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

	static Sak createGsak() {
		// sakId = 1 when persisted.
		return Sak.builder()
				.aktoerId(AKTOER_ID)
				.fagsakNr(GSAK_FAGSAKNR)
				.orgnr(GSAK_ORGNR)
				.tema(GSAK_TEMA)
				.applikasjon(GSAK_APPLIKASJON)
				.opprettetAv(GSAK_OPPRETTET_AV)
				.opprettetTidspunkt(LocalDate.now(FIXED_CLOCK).atStartOfDay())
				.build();
	}

	static UtsendingsInfo createFysiskpostUtsendingsInfo(Journalpost journalpost) {
		var fysiskpostadresse = new UtsendingsInfo.FysiskPostadresse(ADRESSELINJE1, ADRESSELINJE2, ADRESSELINJE3, POSTNUMMER, POSTSTED, LANDKODE_NO);
		return new UtsendingsInfo(journalpost, fysiskpostadresse);
	}

	static UtsendingsInfo createNavNoUtsendingsInfo(Journalpost journalpost) {
		var utsendingsInfoPart = new UtsendingsInfo.NavNoVarsling("navno-identifikator-for-mottaker", "varslingstekst");
		var epostvarsel = new UtsendingsInfo.EpostVarsler(List.of(new UtsendingsInfo.EpostVarsel("tittel", "tekst", "homer@epos.gr", "2023-02-27T12:30:00.000")));
		var smsvarsel = new UtsendingsInfo.SmsVarsler(List.of(new UtsendingsInfo.SmsVarsel("tekst", "+4700000000", "2023-02-27T12:30:00.000")));
		return new UtsendingsInfo(journalpost, utsendingsInfoPart, epostvarsel, smsvarsel);
	}

	static UtsendingsInfo createDigitalPostUtsendingsInfo(Journalpost journalpost) {
		UtsendingsInfo.DigitalPostadresse digitalPostadresse = new UtsendingsInfo.DigitalPostadresse("bjarne.betjent#12AB", "12345678");
		var epostvarsel = new UtsendingsInfo.EpostVarsler(List.of(new UtsendingsInfo.EpostVarsel("tittel", "tekst", "homer@epos.gr", "2023-02-27T12:30:00.000")));
		var smsvarsel = new UtsendingsInfo.SmsVarsler(List.of(new UtsendingsInfo.SmsVarsel("tekst", "+4700000000", "2023-02-27T12:30:00.000")));
		return new UtsendingsInfo(journalpost, digitalPostadresse, epostvarsel, smsvarsel);
	}

	static DateTimeFormatterBuilder formattedDate() {
		return new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.append(ISO_LOCAL_DATE)
				.appendLiteral('T')
				.appendValue(HOUR_OF_DAY, 2)
				.appendLiteral(':')
				.appendValue(MINUTE_OF_HOUR, 2)
				.optionalStart()
				.appendLiteral(':')
				.appendValue(SECOND_OF_MINUTE, 2)
				// ikke avrunding i millis
				.appendFraction(MILLI_OF_SECOND, 3, 3, true);
	}
}

