package no.nav.dokarkiv.hentjournalinfo.gjoark002;

import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapBrukere;
import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapKnyttetDokumentList;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestAssertUtils.assertBrukere;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestAssertUtils.assertJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestAssertUtils.assertKnyttetDokumentList;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createBrukerSet;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createJournalpostDokumentInfoRelasjonSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalpostQueryMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldMapJournalpost() {
        no.nav.dokarkiv.hentjournalinfo.dto.Journalpost journalpost = mapJournalpost(createJournalpost(JOURNALPOST_ID), false);
        assertJournalpost(journalpost);
    }

    @Test
    public void shouldMapKnyttetDokumentList() {
        List<JournalpostDokumentRelasjon> journalpostDokumentInfoRelasjons = mapKnyttetDokumentList(createJournalpostDokumentInfoRelasjonSet(null), 100L, Arrays
                .asList(DOKUMENTINFO_ID));
        assertKnyttetDokumentList(journalpostDokumentInfoRelasjons);
        assertThat(journalpostDokumentInfoRelasjons.get(0).getDokumentInfoId(), is(DOKUMENTINFO_ID));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getJournalpostId(), is(100L));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getSlettet(), is(true));

    }

    @Test
    public void shouldSkipDokumentInfoIfSlettetWhenMappingKnyttetDokumentList() {
        Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonSet = createJournalpostDokumentInfoRelasjonSet(null);
        journalpostDokumentInfoRelasjonSet.add(JournalpostDokumentInfoRelasjon.builder()
                .dokumentInfo(DokumentInfo.builder()
                        .dokumentInfoId(9323L)
                        .slettet(true)
                        .build())
                .build());
        List<JournalpostDokumentRelasjon> journalpostDokumentInfoRelasjons = mapKnyttetDokumentList(journalpostDokumentInfoRelasjonSet, 100L, new ArrayList<>());
        assertThat(journalpostDokumentInfoRelasjons.size(), is(1));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getDokumentInfoId(), is(DOKUMENTINFO_ID));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getJournalpostId(), is(100L));
    }

    @Test
    public void shouldMapBruker() {
        List<no.nav.dokarkiv.hentjournalinfo.dto.Journalpost.Bruker> brukere = mapBrukere(createBrukerSet());
        assertBrukere(brukere);
    }

    @Test
    public void shouldMapWhenBrukerSetIsEmpty() {
        List<no.nav.dokarkiv.hentjournalinfo.dto.Journalpost.Bruker> brukers = mapBrukere(new Journalpost().getBrukere());
        assertThat(brukers.size(), is(0));
    }


    @Test
    public void shouldMapWhenKnyttetDokumentListIsEmpty() {
        List<JournalpostDokumentRelasjon> journalpostDokumentInfoRelasjons = mapKnyttetDokumentList(new Journalpost().getJournalpostDokumentInfoRelasjoner(), 100L, new ArrayList<>());
        assertThat(journalpostDokumentInfoRelasjons.size(), is(0));
    }

}