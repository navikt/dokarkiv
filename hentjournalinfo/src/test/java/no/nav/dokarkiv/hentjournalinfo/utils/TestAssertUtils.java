package no.nav.dokarkiv.hentjournalinfo.utils;

import static no.nav.dokarkiv.core.datautil.BrukerTestDataProvider.BRUKER_ID;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.HOVEDDOKUMENT_TITTEL;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.JOURNALPOST_INNHOLD;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.TILLEGGSOPPLYSNING_KEY;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.TILLEGGSOPPLYSNING_VALUE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostType;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class TestAssertUtils {

    public static void assertDokumentInfo(DokumentInfo dokumentInfo) {
        assertThat(dokumentInfo.getTittel(), is(HOVEDDOKUMENT_TITTEL));
        assertThat(dokumentInfo.getStatus(), is(TestDataUtils.DOKUMENT_STATUS.name()));
        assertThat(dokumentInfo.getTilleggsopplysninger().get(TILLEGGSOPPLYSNING_KEY), is(TILLEGGSOPPLYSNING_VALUE));
        assertThat(dokumentInfo.getKnyttetJournalpostList().get(0).getTilknyttetJournalpostSom(), is(HOVEDDOKUMENT.name()));
        assertThat(dokumentInfo.getKnyttetJournalpostList().get(0).getJournalpost().getTema(), is(TestDataUtils.TEMA.name()));
        assertThat(dokumentInfo.getFilDetaljerList()
                .get(0)
                .getVariantFormat(), is(TestDataUtils.HOVEDDOKUMENT_VARIANTFORMAT.name()));
        assertThat(dokumentInfo.getFilDetaljerList().get(0).getFiltype(), is(TestDataUtils.FIL_TYPE.name()));

        Journalpost journalpost = dokumentInfo.getOriginalJournalpost();
        assertThat(journalpost.getTema(), is(TestDataUtils.TEMA.name()));
        assertThat(journalpost.getTittel(), is(JOURNALPOST_INNHOLD));
        assertThat(journalpost.getJournalpostStatus(), is(JournalpostStatus.mapFromJournalStatusCode(TestDataUtils.JOURNAL_STATUS)));
        assertThat(journalpost.getJournalpostType(), is(JournalpostType.mapFromJournalpostTypeCode(TestDataUtils.JOURNALPOST_TYPE)));
        assertThat(journalpost.getBrukere().get(0).getBrukerId(), is(BRUKER_ID));
        assertThat(journalpost.getBrukere().get(0).getBrukerType(), is(BrukerTypeCode.PERSON.name()));
    }

    public static void assertJournalpost(Journalpost journalpost) {
        assertThat(journalpost.getTema(), is(TestDataUtils.TEMA.name()));
        assertThat(journalpost.getTittel(), is(JOURNALPOST_INNHOLD));
        assertThat(journalpost.getJournalpostStatus(), is(JournalpostStatus.mapFromJournalStatusCode(TestDataUtils.JOURNAL_STATUS)));
        assertThat(journalpost.getJournalpostType(), is(JournalpostType.mapFromJournalpostTypeCode(TestDataUtils.JOURNALPOST_TYPE)));
        assertThat(journalpost.getBrukere().get(0).getBrukerId(), is(BRUKER_ID));
        assertThat(journalpost.getBrukere().get(0).getBrukerType(), is(BrukerTypeCode.PERSON.name()));
        assertThat(journalpost.getKnyttetDokumentList().get(0).getTilknyttetJournalpostSom(), is(HOVEDDOKUMENT.name()));
        assertThat(journalpost.getKnyttetDokumentList().get(0).getDokumentInfo().getTittel(), is(HOVEDDOKUMENT_TITTEL));
        assertThat(journalpost.getKnyttetDokumentList()
                .get(0)
                .getDokumentInfo()
                .getStatus(), is(TestDataUtils.DOKUMENT_STATUS.name()));
    }
}
