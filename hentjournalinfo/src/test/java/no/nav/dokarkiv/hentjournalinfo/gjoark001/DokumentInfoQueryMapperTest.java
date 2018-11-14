package no.nav.dokarkiv.hentjournalinfo.gjoark001;

import static no.nav.dokarkiv.hentjournalinfo.gjoark001.DokumentInfoQueryMapper.mapDokumentInfo;
import static no.nav.dokarkiv.hentjournalinfo.gjoark001.DokumentInfoQueryMapper.mapFildetaljer;
import static no.nav.dokarkiv.hentjournalinfo.gjoark001.DokumentInfoQueryMapper.mapKnyttetJournalpostList;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestAssertUtils.assertJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.DOKUMENT_STATUS;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.FILDETALJER_ID;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.FIL_TYPE;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.HOVEDDOKUMENT_TITTEL;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.HOVEDDOKUMENT_VARIANTFORMAT;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createDokumentInfo;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createFildetaljer;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createJournalpostDokumentInfoRelasjonSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.DokumentStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.FilType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.TilknyttetJournalpostSom;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.VariantFormat;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentInfoQueryMapperTest {

    @Test
    public void shouldMapDokumentInfo() {
        DokumentInfo dokumentInfo = mapDokumentInfo(createDokumentInfo(null));
        assertThat(dokumentInfo.getTittel(), is(HOVEDDOKUMENT_TITTEL));
        assertThat(dokumentInfo.getDokumentStatus(), is(DokumentStatus.mapFromDokumentStatusCode(DOKUMENT_STATUS)));
        assertThat(dokumentInfo.getDokumentInfoId(), is(DOKUMENTINFO_ID));
    }

    @Test
    public void shouldmapKnyttetJournalpostList() {

        List<JournalpostDokumentRelasjon> journalpostDokumentInfoRelasjons = mapKnyttetJournalpostList(createJournalpostDokumentInfoRelasjonSet(createJournalpost(JOURNALPOST_ID)), DOKUMENTINFO_ID, new ArrayList<>(), new ArrayList<>());
        assertThat(journalpostDokumentInfoRelasjons.get(0)
                .getTilknyttetJournalpostSom(), CoreMatchers.is(TilknyttetJournalpostSom.HOVEDDOKUMENT));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getDokumentInfoId(), CoreMatchers.is(DOKUMENTINFO_ID));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getJournalpostId(), CoreMatchers.is(JOURNALPOST_ID));

        assertThat(journalpostDokumentInfoRelasjons.get(0)
                .getJournalpost()
                .getJournalpostId(), CoreMatchers.is(JOURNALPOST_ID));
        assertJournalpost(journalpostDokumentInfoRelasjons.get(0).getJournalpost());
        assertThat(journalpostDokumentInfoRelasjons.get(0).getSlettet(), is(false));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getJournalpost().getSlettet(), is(false));
    }

    @Test
    public void shouldSetSlettetWhenMappingKnyttetJournalpostList() {
        List<JournalpostDokumentRelasjon> journalpostDokumentInfoRelasjons = mapKnyttetJournalpostList(createJournalpostDokumentInfoRelasjonSet(createJournalpost(JOURNALPOST_ID)), DOKUMENTINFO_ID, Arrays
                .asList(JOURNALPOST_ID), Arrays
                .asList(JOURNALPOST_ID));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getSlettet(), is(true));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getJournalpost().getSlettet(), is(true));
    }

    @Test
    public void shouldMapFildetaljerListe() {
        List<DokumentInfo.Fildetaljer> fildetaljerList = mapFildetaljer(createFildetaljer());

        assertThat(fildetaljerList.get(0).getFiltype(), is(FilType.mapFromFilTypeCode(FIL_TYPE)));
        assertThat(fildetaljerList.get(0).getFildetaljerId(), is(FILDETALJER_ID));
        assertThat(fildetaljerList.get(0)
                .getVariantFormat(), is(VariantFormat.mapFromVariantFormatCode(HOVEDDOKUMENT_VARIANTFORMAT)));
    }

    @Test
    public void shouldMapWhenFildetaljerListeIsEmpty() {
        List<DokumentInfo.Fildetaljer> fildetaljerList = mapFildetaljer(no.nav.dokarkiv.core.domain.entities.DokumentInfo.builder()
                .build()
                .getFildetaljerListe());
        assertThat(fildetaljerList.size(), is(0));
    }

    @Test
    public void shouldMapWhenEmptyKnyttetJournalpostList() {
        List<JournalpostDokumentRelasjon> journalpostDokumentInfoRelasjons = mapKnyttetJournalpostList(no.nav.dokarkiv.core.domain.entities.DokumentInfo
                .builder()
                .build()
                .getJournalpostRelasjoner(), DOKUMENTINFO_ID, new ArrayList<>(), new ArrayList<>());
        assertThat(journalpostDokumentInfoRelasjons.size(), is(0));
    }

}