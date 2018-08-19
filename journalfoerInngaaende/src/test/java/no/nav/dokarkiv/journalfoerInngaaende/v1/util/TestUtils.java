package no.nav.dokarkiv.journalfoerInngaaende.v1.util;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
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

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class TestUtils {

	public static final String AVSENDER_NAVN = "avsenderNavn";
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


	public static Journalpost createJournalpost() throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(JournalStatusCode.J)
				.avsenderMottakerId(AVSENDER_ID_PERSON)
				.avsenderMottaker(AVSENDER_NAVN)
				.fagomrade(FagomradeCode.FS22)
				.innhold(INNHOLD)
				.kanalReferanseId(KANALREFERANSE_ID)
				.mottakskanal(MottaksKanalCode.ALTINN)
				.mottattDato(format.parse("2017-02-03T10:37:30.00Z"))
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


	private static JournalpostDokumentInfoRelasjon createJournalpostDokumentinfoRelasjon1() {
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
}
