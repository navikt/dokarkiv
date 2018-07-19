package no.nav.dokarkiv.innsynjournal.v2.datautil;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;

import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

/**
 * Provides helpers for building JournalpostDokumentInfoRelasjon-instances.
 *
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class JournalpostDokumentInfoRelasjonTestDataProvider {

    private JournalpostDokumentInfoRelasjonTestDataProvider() {
    }

    public static JournalpostDokumentInfoRelasjonBuilder createHoveddokumentRelasjon(DokumentInfo dokumentInfo) {
        return createHoveddokumentRelasjon().dokumentInfo(dokumentInfo);
    }

    public static JournalpostDokumentInfoRelasjonBuilder createHoveddokumentRelasjon() {
        return getJournalpostDokumentInfoRelasjonBuilder()
                .tilknyttetAvNavn("testuser")
                .tilknyttetJournalpostSom(HOVEDDOKUMENT)
                .opprettetKildeNavn("test");
    }

    public static JournalpostDokumentInfoRelasjonBuilder createVedleggRelasjon(DokumentInfo dokumentInfo) {
        return createVedleggRelasjon().dokumentInfo(dokumentInfo);
    }

    public static JournalpostDokumentInfoRelasjonBuilder createVedleggRelasjon() {
        return getJournalpostDokumentInfoRelasjonBuilder()
                .tilknyttetAvNavn("testuser")
                .tilknyttetJournalpostSom(VEDLEGG)
                .opprettetKildeNavn("test");
    }
}
