package no.nav.dokarkiv.hentjournalinfo.mock;

import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.BrukerType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.DokumentStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.FilType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.JournalpostType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.Tema;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.TilknyttetJournalpostSom;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.VariantFormat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class MockDataUtils {

    private Long DOKUMENTINFO_ID_HOVEDDOK = 1234L;
    private Long JOURNALPOST_ID_HOVEDDOK = 12345L;
    private Long DOKUMENTINFO_ID_1 = 13455L;
    private Long DOKUMENTINFO_ID_2 = 12155L;
    private Long JOURNALPOST_ID_1 = 22155L;
    private Long JOURNALPOST_ID_2 = 32155L;

    public DokumentInfo createDokumentInfo(Long dokumentInfoId) {
        return DokumentInfo.builder()
                .dokumentInfoId(dokumentInfoId)
                .tittel("Mock tittel for dokument")
                .dokumentStatus(DokumentStatus.FERDIGSTILT)
                .tilleggsopplysninger(createTillegsopplysninger())
                .knyttetJournalpostList(createJournalpostDokumentRelasjonList(JOURNALPOST_ID_HOVEDDOK, dokumentInfoId))
                .filDetaljerList(createFildetaljerList())
                .originalJournalpost(createJournalpost(JOURNALPOST_ID_HOVEDDOK)).build();
    }

    public List<Journalpost.Bruker> createBrukerList(String brukerId) {
        return Arrays.asList(Journalpost.Bruker.builder().brukerType(BrukerType.PERSON).brukerId(brukerId).build());
    }

    public Map<String, String> createTillegsopplysninger() {
        Map<String, String> map = new HashMap<>();
        map.put("bestillingsId", "TOD1233213313K");
        return map;
    }

    public List<DokumentInfo.Fildetaljer> createFildetaljerList() {
        return Arrays.asList(DokumentInfo.Fildetaljer.builder()
                        .variantFormat(VariantFormat.ARKIV)
                        .filtype(FilType.PDF).fildetaljerId(123L)
                        .build(),
                DokumentInfo.Fildetaljer.builder().variantFormat(VariantFormat.PRODUKSJON)
                        .filtype(FilType.AXML).fildetaljerId(124L)
                        .build()
        );
    }

    public List<JournalpostDokumentRelasjon> createJournalpostDokumentRelasjonList(Long hoveddokJournalpostId, Long hoveddokDokumentInfoId) {
        return Arrays.asList(JournalpostDokumentRelasjon.builder()
                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.HOVEDDOKUMENT)
                        .journalpostId(hoveddokJournalpostId)
                        .dokumentInfoId(hoveddokDokumentInfoId)
                        .build(),
                JournalpostDokumentRelasjon.builder()
                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.VEDLEGG)
                        .journalpostId(JOURNALPOST_ID_1)
                        .dokumentInfoId(DOKUMENTINFO_ID_1)
                        .build(),
                JournalpostDokumentRelasjon.builder()
                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.VEDLEGG)
                        .journalpostId(JOURNALPOST_ID_2)
                        .dokumentInfoId(DOKUMENTINFO_ID_2)
                        .build()
        );
    }

    public Journalpost createJournalpost(Long journalpostId) {
        return Journalpost.builder()
                .tema(Tema.FOR)
                .tittel("Mock tittel på journalpost")
                .journalpostStatus(JournalpostStatus.JOURNALFOERT)
                .journalpostType(JournalpostType.INNGAENDE)
                .brukere(createBrukerList("***gammelt_fnr***"))
                .journalpostId(journalpostId)
                .knyttetDokumentList(createJournalpostDokumentRelasjonList(journalpostId, DOKUMENTINFO_ID_HOVEDDOK))
                .build();
    }
}
