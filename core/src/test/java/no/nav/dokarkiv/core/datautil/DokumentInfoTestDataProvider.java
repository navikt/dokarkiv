package no.nav.dokarkiv.core.datautil;

import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.createFilDetaljerArkivPDFA;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.createFilDetaljerProduksjonXML;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.createFilDetaljerSladdetPDFA;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;

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
		return createDokumentInfo(DOKUMENT_TITTEL, DokumentFilTestDataProvider.FIL_UUID, DokumentFilTestDataProvider.FIL_UUID_SLADDET);
	}

	public static DokumentInfoBuilder createDokumentInfo(String dokumentTittel, String filuid, String filuidSladdet) {
		return createDokumentInfo(dokumentTittel,
				getFilDetaljerBuilder().filtype(PDF)
						.filUuid(filuid).variantFormat(ARKIV)
						.opprettetKildeNavn("test"), filuidSladdet);
	}

	public static DokumentInfoBuilder createDokumentInfo(String dokumentTittel, FilDetaljerBuilder filDetaljerBuilder, String filuidSladdet) {
		return getDokumentInfoBuilder()
				.opprettetKildeNavn("test")
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(dokumentTittel)
				.skannetInnhold(SkannetInnholdTestDataProvider.createSkannetInnhold().build())
				.filDetaljerList(filDetaljerBuilder.build(), getFilDetaljerBuilder().variantFormat(SLADDET).filUuid(filuidSladdet).filtype(PDF).opprettetKildeNavn("test").build());
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
				.filDetaljerList(createFilDetaljerArkivPDFA(), createFilDetaljerProduksjonXML(),createFilDetaljerSladdetPDFA())
				.skannetInnhold(SkannetInnholdTestDataProvider.createSkannetInnhold().build());
	}

	public static DokumentInfoBuilder createVedleggDokumentInfo() {
		return getDokumentInfoBuilder()
				.kategori(DokumentKategoriCode.ES)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.dokumenttypeId("I0002")
				.tittel("Takk skal du ha")
				.opprettetKildeNavn("itest")
				.filDetaljerList(createFilDetaljerArkivPDFA(), createFilDetaljerProduksjonXML(), createFilDetaljerSladdetPDFA())
				.skannetInnhold(SkannetInnholdTestDataProvider.createSkannetInnhold().build());
	}


}
