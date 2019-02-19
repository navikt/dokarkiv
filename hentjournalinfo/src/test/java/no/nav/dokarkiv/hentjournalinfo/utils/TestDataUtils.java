package no.nav.dokarkiv.hentjournalinfo.utils;

import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder;
import no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class TestDataUtils {

    public final static byte[] FILE = "Testdata".getBytes();
    public final static String TILLEGGSOPPLYSNING_KEY = "bestillingsId";
    public final static String TILLEGGSOPPLYSNING_VALUE = "123345";
    public final static String REFERANSEID = "refId";
    public final static String HOVEDDOKUMENT_TITTEL = "hoveddok_tittel";
    public final static String VEDLEGG_TITTEL = "vedlegg_tittel";
    public final static String KILDE_NAVN = "testuser";
    public final static String OPPRETTET_AV_NAVN = "testuser";
    public final static String JOURNALPOST_INNHOLD = "journalpost tittel";
    public final static FagomradeCode TEMA = FagomradeCode.PEN;
    public final static JournalStatusCode JOURNAL_STATUS = JournalStatusCode.FS;
    public final static JournalpostTypeCode JOURNALPOST_TYPE = JournalpostTypeCode.I;
    public final static ReferanseTypeCode REFERANSE_TYPE = ReferanseTypeCode.SED_FLYT;
    public final static MottaksKanalCode MOTTAKS_KANAL = MottaksKanalCode.NAV_NO;
    public final static DokumentStatusCode DOKUMENT_STATUS = DokumentStatusCode.FERDIGSTILT;
    public final static FilTypeCode FIL_TYPE = FilTypeCode.PDF;
    public final static VariantFormatCode HOVEDDOKUMENT_VARIANTFORMAT = VariantFormatCode.ARKIV;
    public final static Long DOKUMENTINFO_ID = 1133L;
    public final static Long JOURNALPOST_ID = 1134L;
    public final static Long FILDETALJER_ID = 1134L;

    public static Set<JournalpostDokumentInfoRelasjon> createJournalpostDokumentInfoRelasjonSet(Journalpost journalpost) {
        return new HashSet<>(Arrays.asList(JournalpostDokumentInfoRelasjon.builder()
                .tilknyttetJournalpostSom(HOVEDDOKUMENT)
                .tilknyttetAvNavn(OPPRETTET_AV_NAVN)
                .journalpost(createJournalpost(JOURNALPOST_ID))
                .dokumentInfo(createDokumentInfo(journalpost)).build()));
    }

    public static JournalpostBuilder createJournalpostBuilder(String filUuid) {
        return JournalpostBuilder
                .getJournalpostBuilder()
                .fagomrade(TEMA)
                .innhold(JOURNALPOST_INNHOLD)
                .journalStatus(JOURNAL_STATUS)
                .journalpostType(JOURNALPOST_TYPE)
                .opprettetAvNavn(OPPRETTET_AV_NAVN)
                .opprettetKildeNavn(KILDE_NAVN)
                .addOriginalJournalpost(true)
                .tilleggsopplysninger(createTilleggsopplysninger())
                .kryssReferanser(KryssreferanseBuilder.getKryssreferanseBuilder()
                        .referanseId(REFERANSEID)
                        .referanseType(REFERANSE_TYPE)
                        .opprettetKildeNavn(KILDE_NAVN)
                        .build())
                .saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon().build())
                .brukere(BrukerTestDataProvider.createBruker().build())
                .mottakskanal(MOTTAKS_KANAL)
                .dokumentInfoRelasjoner(
                        JournalpostDokumentInfoRelasjonBuilder
                                .getJournalpostDokumentInfoRelasjonBuilder()
                                .tilknyttetAvNavn(OPPRETTET_AV_NAVN)
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
                                .opprettetKildeNavn(KILDE_NAVN)
                                .dokumentInfo(
                                        DokumentInfoBuilder
                                                .getDokumentInfoBuilder()
                                                .opprettetKildeNavn(KILDE_NAVN)
                                                .dokumentstatus(DOKUMENT_STATUS)
                                                .tittel(HOVEDDOKUMENT_TITTEL)
                                                .tilleggsopplysninger(createTilleggsopplysninger())
                                                .skannetInnhold(SkannetInnholdBuilder.getSkannetInnholdBuilder()
                                                        .vedleggNr(1)
                                                        .opprettetKildeNavn(KILDE_NAVN)
                                                        .build())
                                                .filDetaljerList(
                                                        FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FIL_TYPE)
                                                                .filUuid(filUuid).variantFormat(HOVEDDOKUMENT_VARIANTFORMAT)
                                                                .opprettetKildeNavn(KILDE_NAVN).build()).build()).build())
                .dokumentInfoRelasjoner(
                        JournalpostDokumentInfoRelasjonBuilder
                                .getJournalpostDokumentInfoRelasjonBuilder()
                                .tilknyttetAvNavn(OPPRETTET_AV_NAVN)
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                                .opprettetKildeNavn(KILDE_NAVN)
                                .dokumentInfo(
                                        DokumentInfoBuilder
                                                .getDokumentInfoBuilder()
                                                .opprettetKildeNavn(KILDE_NAVN)
                                                .dokumentstatus(DOKUMENT_STATUS)
                                                .tittel(VEDLEGG_TITTEL)
                                                .tilleggsopplysninger(createTilleggsopplysninger())
                                                .skannetInnhold(SkannetInnholdBuilder.getSkannetInnholdBuilder()
                                                        .vedleggNr(1)
                                                        .opprettetKildeNavn(KILDE_NAVN)
                                                        .build())
                                                .filDetaljerList(
                                                        FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FIL_TYPE)
                                                                .filUuid(filUuid).variantFormat(HOVEDDOKUMENT_VARIANTFORMAT)
                                                                .opprettetKildeNavn(KILDE_NAVN).build()).build()).build());
    }


    public static Map<String, String> createTilleggsopplysninger() {
        Map<String, String> map = new HashMap<>();
        map.put(TILLEGGSOPPLYSNING_KEY, TILLEGGSOPPLYSNING_VALUE);
        return map;
    }

    public static Set<Bruker> createBrukerSet() {
        Set<Bruker> brukers = new HashSet<>();
        brukers.add(BrukerTestDataProvider.createBruker().build());
        return brukers;
    }

    public static DokumentInfo createDokumentInfo(Journalpost journalpost) {
        return DokumentInfo.builder()
                .dokumentInfoId(DOKUMENTINFO_ID)
                .tittel(HOVEDDOKUMENT_TITTEL)
                .dokumentstatus(DOKUMENT_STATUS)
                .originalJournalpost(journalpost)
                .fildetaljerListe(createFildetaljer())
                .build();
    }

    public static Set<FilDetaljer> createFildetaljer() {
        Set<FilDetaljer> filDetaljerSet = new HashSet<>();
        filDetaljerSet.add(FilDetaljer.builder()
                .fildetaljerId(FILDETALJER_ID)
                .filtype(FIL_TYPE)
                .filUuid(FIL_UUID).variantFormat(HOVEDDOKUMENT_VARIANTFORMAT)
                .build());
        return filDetaljerSet;
    }

    public static Journalpost createJournalpost(Long journalpostId) {
        return Journalpost.builder()
                .journalpostId(journalpostId)
                .fagomrade(TEMA)
                .innhold(JOURNALPOST_INNHOLD)
                .journalstatus(JOURNAL_STATUS)
                .journalposttype(JOURNALPOST_TYPE)
                .opprettetAvNavn(OPPRETTET_AV_NAVN)
                .tilleggsopplysninger(createTilleggsopplysninger())
                .saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon().build())
                .mottakskanal(MOTTAKS_KANAL).build();
    }
}
