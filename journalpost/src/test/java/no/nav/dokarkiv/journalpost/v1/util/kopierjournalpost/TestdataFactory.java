package no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost;


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
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.now;
import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode.FNR;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.ES;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.JSON;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;

public class TestdataFactory {
	static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	static final String AVSENDER_MOTTAKER_ID = "02016126007";
	static final String AVSENDER_MOTTAKER_NAVN = "Jim Hopper";
	static final String AVSENDER_MOTTAKER_LAND = "NO";
	static final AvsenderMottakerIdTypeCode AVSENDER_MOTTAKER_ID_TYPE = FNR;
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
	static final String KANAL_REFERANSE_ID = "KANAL_REFERANSE_ID";
	static final String SKANNET_INNHOLD_TITTEL = "Henvendelse fra lege";
	static final String BEHANDLINGSTEMA = "ab0438";
	static final String JOURNALFOERENDE_ENHET = "9999";
	static final String JOURNALFOERT_AV_NAVN = "Bjarne Betjent";
	static final SkjermingTypeCode SKJERMING_TYPE_CODE = POL;
	static final String FIL_UUID_ARKIV_HOVEDDOKUMENT = "filUuidHoveddokumentArkiv";
	static final String FIL_UUID_PRODUKSJON_HOVEDDOKUMENT = "filUuidHoveddokumentProduksjon";
	static final String FIL_UUID_ARKIV_VEDLEGG = "filUuidVedleggArkiv";
	static final String FIL_UUID_PRODUKSJON_VEDLEGG = "filUuidVedleggProduksjon";
	static final Long SAK_ID = 12223344L;
	private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2023-08-11T12:01:01.001Z"), ZONEID_NORGE);

	public static Journalpost createJournalpostWithHoveddokumentAndVedlegg(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus) {
		Journalpost journalpost = Journalpost.builder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottaker(AVSENDER_MOTTAKER_NAVN)
				.avsenderMottakerIdType(AVSENDER_MOTTAKER_ID_TYPE)
				.land(AVSENDER_MOTTAKER_LAND)
				.mottattDato(LocalDateTime.now(FIXED_CLOCK))
				.journalDato(Date.from(now(FIXED_CLOCK)))
				.sendtPrintDato(Date.from(now(FIXED_CLOCK)))
				.ekspedertDato(Date.from(now(FIXED_CLOCK)))
				.avsendtReturDato(Date.from(now(FIXED_CLOCK)))
				.dokumentDato(Date.from(now(FIXED_CLOCK)))
				.lestDato(Date.from(now(FIXED_CLOCK)))
				.utsendingskanal(NAV_NO)
				.journalstatus(journalStatus)
				.journalposttype(journalpostType)
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
		journalpost.setSaksrelasjon(createSaksrelasjon(journalpost));
		journalpost.setTilleggsopplysninger(createTilleggsopplysninger());
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

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

	static no.nav.dokarkiv.core.domain.entities.Saksrelasjon createSaksrelasjon(Journalpost journalpost) {
		no.nav.dokarkiv.core.domain.entities.Saksrelasjon saksrelasjon = no.nav.dokarkiv.core.domain.entities.Saksrelasjon.builder()
				.fagsystem(FagsystemCode.FS22)
				.sakId(SAK_ID)
				.journalpost(journalpost)
				.feilregistrert(false)
				.build();
		saksrelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return saksrelasjon;
	}

	static Bruker createBruker() {
		Bruker bruker = Bruker.builder()
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
				.dokumentFerdigDato(Date.from(now(FIXED_CLOCK)))
				.brevkode(BREVKODE)
				.kassert(false)
				.kategori(ES)
				.sensitivt(true)
				.build();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, ARKIV, PDF, filUuidArkiv));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, PRODUKSJON, JSON, filUuidProduksjon));
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
}

