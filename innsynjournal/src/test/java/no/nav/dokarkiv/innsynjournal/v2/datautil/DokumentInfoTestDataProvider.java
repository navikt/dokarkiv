package no.nav.dokarkiv.innsynjournal.v2.datautil;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.innsynjournal.v2.datautil.FildetaljerTestDataProvider.createFilDetaljerArkivPDFA;
import static no.nav.dokarkiv.innsynjournal.v2.datautil.FildetaljerTestDataProvider.createFilDetaljerProduksjonXML;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;

/**
 * Provides helpers for building {@link DokumentInfoBuilder}-instances
 *
 * @author Roar Bjurstrom, Visma Consulting.
 * @author Thomas Kåsene, Visma Consulting AS
 */
public final class DokumentInfoTestDataProvider {

	private DokumentInfoTestDataProvider() {
	}

	public static final String DOKUMENT_TITTEL = "Dokumenttittel";

	public static DokumentInfoBuilder createDokumentInfo() {
		return createDokumentInfo(DOKUMENT_TITTEL, DokumentFilTestDataProvider.FIL_UUID);
	}

	public static DokumentInfoBuilder createDokumentInfo(String dokumentTittel, String filuid) {
		return createDokumentInfo(dokumentTittel,
                getFilDetaljerBuilder().filtype(PDF)
                        .filUuid(filuid).variantFormat(ARKIV)
                        .opprettetKildeNavn("test"));
	}

	public static DokumentInfoBuilder createDokumentInfo(String dokumentTittel, FilDetaljerBuilder filDetaljerBuilder) {
		return getDokumentInfoBuilder()
				.opprettetKildeNavn("test")
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(dokumentTittel)
				.skannetInnhold(SkannetInnholdTestDataProvider.createSkannetInnhold().build())
				.filDetaljerList(filDetaljerBuilder.build());
	}

    public static DokumentInfoBuilder createDokumentInfo(DokumentKategoriCode kategori) {
        return createDokumentInfo()
                .kategori(kategori);
    }

	public static DokumentInfoBuilder createHovedDokumentInfoFP() {
		return getDokumentInfoBuilder()
				.kategori(DokumentKategoriCode.SOK)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.dokumenttypeId("I0001")
				.tittel("Gi meg foreldrepenger")
				.opprettetKildeNavn("itest")
				.filDetaljerList(createFilDetaljerArkivPDFA(), createFilDetaljerProduksjonXML());
	}

	public static DokumentInfoBuilder createVedleggDokumentInfo() {
		return getDokumentInfoBuilder()
				.kategori(DokumentKategoriCode.ES)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.dokumenttypeId("I0002")
				.tittel("Takk skal du ha")
				.opprettetKildeNavn("itest")
				.filDetaljerList(createFilDetaljerArkivPDFA(), createFilDetaljerProduksjonXML());
	}


}
