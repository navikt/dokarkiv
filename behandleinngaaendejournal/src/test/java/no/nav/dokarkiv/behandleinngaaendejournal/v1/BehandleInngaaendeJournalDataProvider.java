package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class BehandleInngaaendeJournalDataProvider {
	public static final long DOKUMENT_INFO_ID = 1L;
	public static final LocalDateTime now = LocalDateTime.now();
	public static final String FNR = "***gammelt_fnr***";
	public static final String DOKUMENTTYPE_ID = "I00008";
	public static final String INNHOLD = "Say NO to porridge";
	public static final String TITTEL = "Indrefilet av hest";
	public static final String OPPRETTET_AV_NAVN = "opprettetAvNavn";
	private static final GregorianCalendar DATE2 = new GregorianCalendar(2017, 1, 2);
	public static final String AVSENDER_MOTTAKERID = "***gammelt_fnr***";
	public static final String ARKIV_SAKID = "1";
	public static final Long DOKUMENT_INFO_ID_VEDLEGG = 2L;
	public static final String ORGNR = "916214588";
	public static final String AVSENDER_MOTTAKER_NAVN = "Batman";
	public static final DokumentKategoriCode HOVEDDOKUMENT_KATEGORI_KODE = DokumentKategoriCode.SOK;
	public static final DokumentKategoriCode VEDLEGG_KATEGORI_KODE = DokumentKategoriCode.ES;
	public static final FagsystemCode ARKIV_SAK_FAGSYSTEM = FagsystemCode.PEN;
	public static final FagomradeCode JOURNALPOST_FAGOMRADE = FagomradeCode.PEN;
	
	private static final String FNR2 = "***gammelt_fnr***";
	private static final String CREATED_BY = "Roark Bjoarkstrøm";
	private static final GregorianCalendar DATE1 = new GregorianCalendar(2017, 1, 1);
	private static final GregorianCalendar DATE3 = new GregorianCalendar(2016, 12, 15);
	private static final GregorianCalendar DATE4 = new GregorianCalendar(2016, 11, 16);
	public static final String ORGNR2 = "923609016";
	private static final String DOKUMENTTYPE_ID_VEDLEGG = "I00024";
	private static final DokumentStatusCode DOKUMENT_STATUS_KODE = DokumentStatusCode.FERDIGSTILT;
	
	public static JournalpostBuilder buildJournalpost() {
		return buildBasicJournalpost()
				.brukere(createBruker(FNR, BrukerTypeCode.PERSON, DATE1.getTime()),
						createBruker(ORGNR, BrukerTypeCode.ORGANISASJON, DATE2.getTime()),
						createBruker(ORGNR2, BrukerTypeCode.ORGANISASJON, DATE3.getTime()),
						createBruker(FNR2, BrukerTypeCode.PERSON, DATE4.getTime()))
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createHovedDokumentInfo().build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(createVedleggDokumentInfo(DOKUMENT_INFO_ID_VEDLEGG).build())
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.build());
		
	}

	public static JournalpostBuilder buildBasicJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottaker(AVSENDER_MOTTAKER_NAVN)
				.avsenderMottakerId(AVSENDER_MOTTAKERID)
				.mottattDato(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
				.dokumentDato(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.fagomrade(JOURNALPOST_FAGOMRADE)
				.journalStatus(JournalStatusCode.M)
				.journalpostType(JournalpostTypeCode.I)
				.saksrelasjon(createSaksrelasjon())
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.innhold(INNHOLD);
	}

	public static JournalpostBuilder buildNoRelasjonJournalpost() {
		return buildBasicJournalpost()
				.brukere(createBruker(FNR, BrukerTypeCode.PERSON, DATE1.getTime()),
						createBruker(ORGNR, BrukerTypeCode.ORGANISASJON, DATE2.getTime()));
	}

	public static JournalpostBuilder buildNoBrukerJournalpost() {
		return buildBasicJournalpost()
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createHovedDokumentInfo().build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createVedleggDokumentInfo(DOKUMENT_INFO_ID_VEDLEGG).build())
								.build());
	}

	public static Saksrelasjon createSaksrelasjon() {
		return getSaksrelasjonBuilder()
				.sakId(ARKIV_SAKID)
				.fagsystem(ARKIV_SAK_FAGSYSTEM)
				.build();
	}

	public static Bruker createBruker(String id, BrukerTypeCode brukerTypeCode, Date date) {
		return getBrukerBuilder()
				.brukerId(id)
				.brukerType(brukerTypeCode)
				.changeStamp(new ChangeStamp(CREATED_BY, date, null, null))
				.build();
	}

	public static DokumentInfoBuilder createHovedDokumentInfo() {
		return createHoveddokumentInfoNoFildetaljer()
				.filDetaljerList(createFilDetaljerArkiv(), createFilDetaljerProduksjon());
	}

	public static DokumentInfoBuilder createVedleggDokumentInfo(long dokumentInfoId) {
		return createVedleggDokumentInfoNoFildetaljer(dokumentInfoId)
				.filDetaljerList(createFilDetaljerArkiv(), createFilDetaljerProduksjon());
	}

	public static DokumentInfoBuilder createHoveddokumentInfoNoFildetaljer() {
		return getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_INFO_ID)
				.kategori(HOVEDDOKUMENT_KATEGORI_KODE)
				.dokumentstatus(DOKUMENT_STATUS_KODE)
				.dokumenttypeId(DOKUMENTTYPE_ID)
				.tittel(TITTEL)
				.sensitivt(false);
	}

	public static DokumentInfoBuilder createVedleggDokumentInfoNoFildetaljer(long dokumentInfoId) {
		return getDokumentInfoBuilder()
				.dokumentInfoId(dokumentInfoId)
				.kategori(VEDLEGG_KATEGORI_KODE)
				.dokumentstatus(DOKUMENT_STATUS_KODE)
				.dokumenttypeId(DOKUMENTTYPE_ID_VEDLEGG)
				.tittel(TITTEL)
				.sensitivt(false);
	}

	public static FilDetaljer createFilDetaljerArkiv() {
		return getFilDetaljerBuilder()
				.filtype(FilTypeCode.PDFA)
				.variantFormat(VariantFormatCode.ARKIV)
				.build();
	}

	public static FilDetaljer createFilDetaljerProduksjon() {
		return getFilDetaljerBuilder()
				.filtype(FilTypeCode.XML)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.build();
	}
}
