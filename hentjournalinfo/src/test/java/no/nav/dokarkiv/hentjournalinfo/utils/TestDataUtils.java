package no.nav.dokarkiv.hentjournalinfo.utils;

import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.VARIANT_FORMAT;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder;
import no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class TestDataUtils {

    public final static byte[] FILE = "Testdata".getBytes();
    public final static String TILLEGGSOPPLYSNING_KEY = "bestillingsId";
    public final static String TILLEGGSOPPLYSNING_VAL = "123345";
    public final static String REFERANSEID = "refId";
    public final static String HOVEDDOKUMENT_TITTEL = "hoveddok_tittel";


    public static JournalpostBuilder createJournalpostBuilder() {
        return JournalpostBuilder
                .getJournalpostBuilder()
                .fagomrade(FagomradeCode.PEN)
                .journalStatus(JournalStatusCode.FS)
                .journalpostType(JournalpostTypeCode.U)
                .opprettetAvNavn("testuser")
                .opprettetKildeNavn("test")
                .tilleggsopplysninger(createTilleggsopplysninger())
                .kryssReferanser(KryssreferanseBuilder.getKryssreferanseBuilder()
                        .referanseId(REFERANSEID)
                        .referanseType(ReferanseTypeCode.SED_FLYT)
                        .opprettetKildeNavn("TEST")
                        .build())
                .saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon().build())
                .brukere(BrukerTestDataProvider.createBruker().build())
                .mottakskanal(MottaksKanalCode.NAV_NO)
                .dokumentInfoRelasjoner(
                        JournalpostDokumentInfoRelasjonBuilder
                                .getJournalpostDokumentInfoRelasjonBuilder()
                                .tilknyttetAvNavn("testuser")
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
                                .opprettetKildeNavn("test")
                                .dokumentInfo(
                                        DokumentInfoBuilder
                                                .getDokumentInfoBuilder()
                                                .opprettetKildeNavn("test")
                                                .dokumentstatus(DokumentStatusCode.FERDIGSTILT)
                                                .tittel(HOVEDDOKUMENT_TITTEL)
                                                .tilleggsopplysninger(createTilleggsopplysninger())

                                                .skannetInnhold(SkannetInnholdBuilder.getSkannetInnholdBuilder()
                                                        .vedleggNr(1)
                                                        .opprettetKildeNavn("test")
                                                        .build())
                                                .filDetaljerList(
                                                        FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
                                                                .filUuid(FIL_UUID).variantFormat(VARIANT_FORMAT)
                                                                .opprettetKildeNavn("test").build()).build()).build());
    }

    public static DokumentFilBuilder createDokumentFilBuilder() {
        return DokumentFilBuilder.getDokumentFilBuilder().fil(FILE).opprettetKildeNavn("boie").filUuid(FIL_UUID);
    }

    public static Map<String, String> createTilleggsopplysninger() {
        Map<String, String> map = new HashMap<>();
        map.put(TILLEGGSOPPLYSNING_KEY, TILLEGGSOPPLYSNING_VAL);
        return map;
    }


}
