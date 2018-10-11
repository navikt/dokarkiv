package no.nav.dokarkiv.hentjournalinfo.utils;

import static no.nav.dokarkiv.core.datautil.BrukerTestDataProvider.BRUKER_ID;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.DOKUMENT_STATUS;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.FIL_TYPE;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.HOVEDDOKUMENT_TITTEL;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.HOVEDDOKUMENT_VARIANTFORMAT;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.JOURNALPOST_INNHOLD;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.TEMA;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.TILLEGGSOPPLYSNING_KEY;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.TILLEGGSOPPLYSNING_VALUE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class TestAssertUtils {

    public static void assertDokumentInfo(DokumentInfo dokumentInfo) {
        assertThat(dokumentInfo.getTittel(), is(HOVEDDOKUMENT_TITTEL));
        assertThat(dokumentInfo.getDokumentStatus(), is(DokumentStatus.mapFromDokumentStatusCode(DOKUMENT_STATUS)));
        assertThat(dokumentInfo.getTilleggsopplysninger().get(TILLEGGSOPPLYSNING_KEY), is(TILLEGGSOPPLYSNING_VALUE));
        assertThat(dokumentInfo.getKnyttetJournalpostList()
                .get(0)
                .getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSom.mapTilknyttetJournalpostSomCode(HOVEDDOKUMENT)));
        assertThat(dokumentInfo.getKnyttetJournalpostList()
                .get(0)
                .getJournalpost()
                .getTema(), is(Tema.mapFromFagomradeCode(TEMA)));
        assertThat(dokumentInfo.getFilDetaljerList()
                .get(0)
                .getVariantFormat(), is(VariantFormat.mapFromVariantFormatCode(HOVEDDOKUMENT_VARIANTFORMAT)));
        assertThat(dokumentInfo.getFilDetaljerList().get(0).getFiltype(), is(FilType.mapFromFilTypeCode(FIL_TYPE)));

        Journalpost journalpost = dokumentInfo.getOriginalJournalpost();
        assertJournalpost(journalpost);
        assertBrukere(journalpost.getBrukere());
    }

    public static void assertBrukere(List<Journalpost.Bruker> bruker) {
        assertThat(bruker.get(0).getBrukerId(), is(BRUKER_ID));
        assertThat(bruker.get(0).getBrukerType(), is(BrukerType.PERSON));
    }

    public static void assertKnyttetDokumentList(List<JournalpostDokumentRelasjon> dokumentRelasjons) {
        assertThat(dokumentRelasjons.get(0)
                .getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSom.mapTilknyttetJournalpostSomCode(HOVEDDOKUMENT)));
        assertThat(dokumentRelasjons.get(0).getDokumentInfo().getTittel(), is(HOVEDDOKUMENT_TITTEL));
        assertThat(dokumentRelasjons
                .get(0)
                .getDokumentInfo()
                .getDokumentStatus(), is(DokumentStatus.mapFromDokumentStatusCode(DOKUMENT_STATUS)));
    }

    public static void assertJournalpost(Journalpost journalpost) {
        assertThat(journalpost.getTema(), is(Tema.mapFromFagomradeCode(TEMA)));
        assertThat(journalpost.getTittel(), is(JOURNALPOST_INNHOLD));
        assertThat(journalpost.getJournalpostStatus(), is(JournalpostStatus.mapFromJournalStatusCode(TestDataUtils.JOURNAL_STATUS)));
        assertThat(journalpost.getJournalpostType(), is(JournalpostType.mapFromJournalpostTypeCode(TestDataUtils.JOURNALPOST_TYPE)));
    }
}
