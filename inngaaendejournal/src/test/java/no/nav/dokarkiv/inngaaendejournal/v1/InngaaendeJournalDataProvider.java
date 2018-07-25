package no.nav.dokarkiv.inngaaendejournal.v1;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;

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
import org.joda.time.LocalDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class InngaaendeJournalDataProvider {
	public static final String AVSENDER_MOTTAKERID = "***gammelt_fnr***";
	public static final long DOKUMENT_INFO_ID = 1L;
	public static final String ARKIV_SAKID = "1";
	public static final LocalDateTime NOW = LocalDateTime.now();
	public static final String FNR = "***gammelt_fnr***";
	public static final String ORGNR = "999999999";
	public static final String DOKUMENTTYPE_ID = "I00008";
	public static final Long DOKUMENT_INFO_ID_VEDLEGG = 2L;
	public static final Long DOKUMENT_INFO_ID_VEDLEGG_2 = 3L;
	public static final Long DOKUMENT_INFO_ID_VEDLEGG_3 = 4L;
	public static final String DOKUMENTTYPE_ID_VEDLEGG = "I00024";
	public static final String INNHOLD = "Mitt innhold";
	private static final String AVSENDER_MOTTAKERNAVN = "Spiderman";
	public static final String TITTEL_VEDLEGG = "Mitt vedlegg";
	private static final String TITTEL_HOVEDDOKUMENT = "Mitt hoveddokument";

	public static JournalpostBuilder buildBaseJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottakerId(AVSENDER_MOTTAKERID)
				.mottattDato(NOW.toDate())
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.fagomrade(FagomradeCode.PEN)
				.journalStatus(JournalStatusCode.J)
				.journalpostType(JournalpostTypeCode.I)
				.saksrelasjon(createSaksrelasjon())
				.brukere(createBruker(FNR, BrukerTypeCode.PERSON), createBruker(ORGNR, BrukerTypeCode.ORGANISASJON))
				.innhold(INNHOLD)
				.avsenderMottaker(AVSENDER_MOTTAKERNAVN)
				.dokumentDato(NOW.toDate());
	}

	public static JournalpostBuilder buildJournalpost() {
		return buildBaseJournalpost()
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(createHovedDokumentInfo().build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(createVedleggDokumentInfo().build())
								.build());

	}

	public static Saksrelasjon createSaksrelasjon() {
		return getSaksrelasjonBuilder()
				.sakId(ARKIV_SAKID)
				.fagsystem(FagsystemCode.PEN)
				.build();
	}

	public static Bruker createBruker(String id, BrukerTypeCode brukerTypeCode) {
		return getBrukerBuilder()
				.brukerId(id)
				.brukerType(brukerTypeCode)
				.build();
	}

	public static DokumentInfoBuilder createBaseHovedDokumentInfo() {
		return getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_INFO_ID)
				.kategori(DokumentKategoriCode.SOK)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.dokumenttypeId(DOKUMENTTYPE_ID)
				.tittel(TITTEL_HOVEDDOKUMENT);
	}

	public static DokumentInfoBuilder createHovedDokumentInfo() {
		return createBaseHovedDokumentInfo()
				.filDetaljerList(createFilDetaljerArkiv(), createFilDetaljerProduksjon());
	}

	public static DokumentInfoBuilder createBaseVedleggDokumentInfo() {
		return getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG)
				.kategori(DokumentKategoriCode.ES)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.dokumenttypeId(DOKUMENTTYPE_ID_VEDLEGG)
				.tittel(TITTEL_VEDLEGG);
	}

	public static DokumentInfoBuilder createVedleggDokumentInfo() {
		return createBaseVedleggDokumentInfo()
				.filDetaljerList(createFilDetaljerArkiv(), createFilDetaljerProduksjon());
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
