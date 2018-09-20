package no.nav.dokarkiv.hentjournalinfo.gjoark002;

import static no.nav.dokarkiv.core.datautil.BrukerTestDataProvider.BRUKER_ID;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapBrukere;
import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapKnyttetDokumentList;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.DOKUMENT_STATUS;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.HOVEDDOKUMENT_TITTEL;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.JOURNALPOST_INNHOLD;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.JOURNALPOST_TYPE;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.JOURNAL_STATUS;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.TEMA;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createJournalpostBuilder;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.createJournalpostDokumentInfoRelasjon;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashSet;
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
        no.nav.dokarkiv.hentjournalinfo.dto.Journalpost journalpost = mapJournalpost(createJournalpostBuilder("test").build());
        assertThat(journalpost.getJournalpostId(), is(1L));
        assertThat(journalpost.getTema(), is(TEMA.name()));
        assertThat(journalpost.getTittel(), is(JOURNALPOST_INNHOLD));
        assertThat(journalpost.getJournalpostType(), is(JournalpostType.mapFromJournalpostTypeCode(JOURNALPOST_TYPE)));
        assertThat(journalpost.getJournalpostStatus(), is(JournalpostStatus.mapFromJournalStatusCode(JOURNAL_STATUS)));
    }

    @Test
    public void shouldMapKnyttetDokumentList() {
        List<JournalpostDokumentRelasjon> journalpostDokumentInfoRelasjons = mapKnyttetDokumentList(createJournalpostDokumentInfoRelasjon(), 100L);
        assertThat(journalpostDokumentInfoRelasjons.get(0).getTilknyttetJournalpostSom(), is(HOVEDDOKUMENT.name()));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getDokumentInfoId(), is(1L));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getJournalpostId(), is(100L));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getDokumentInfo().getStatus(), is(DOKUMENT_STATUS.name()));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getDokumentInfo().getTittel(), is(HOVEDDOKUMENT_TITTEL));
    }

    @Test
    public void shouldSkipDokumentInfoIfSlettetWhenMappingKnyttetDokumentList() {
        Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonSet = createJournalpostDokumentInfoRelasjon();
        journalpostDokumentInfoRelasjonSet.add(JournalpostDokumentInfoRelasjon.builder()
                .dokumentInfo(DokumentInfo.builder()
                        .dokumentInfoId(9323L)
                        .slettet(true)
                        .build())
                .build());
        List<JournalpostDokumentRelasjon> journalpostDokumentInfoRelasjons = mapKnyttetDokumentList(journalpostDokumentInfoRelasjonSet, 100L);
        assertThat(journalpostDokumentInfoRelasjons.size(), is(1));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getDokumentInfoId(), is(1L));
        assertThat(journalpostDokumentInfoRelasjons.get(0).getJournalpostId(), is(100L));
    }

    @Test
    public void shouldMapBruker() {
        List<no.nav.dokarkiv.hentjournalinfo.dto.Journalpost.Bruker> brukers = mapBrukere(createBrukerSet());
        assertThat(brukers.get(0).getBrukerType(), is("PERSON"));
        assertThat(brukers.get(0).getBrukerId(), is(BRUKER_ID));
    }

    @Test
    public void shouldMapWhenBrukerSetIsEmpty() {
        List<no.nav.dokarkiv.hentjournalinfo.dto.Journalpost.Bruker> brukers = mapBrukere(new Journalpost().getBrukere());
        assertThat(brukers.size(), is(0));
    }


    @Test
    public void shouldThrowWhenJournalpostStatusIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Kunne ikke mappe JournalStatusCode=null til JournalpostStatus");
        Journalpost journalpost = createJournalpostBuilder("test").build();
        journalpost.setJournalstatus(null);
        mapJournalpost(journalpost);
    }

    @Test
    public void shouldThrowWhenJournalpostTypeIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Kunne ikke mappe JournalpostTypeCode=null til JournalpostType");

        Journalpost journalpost = createJournalpostBuilder("test").build();
        journalpost.setJournalposttype(null);
        mapJournalpost(journalpost);
    }

    private Set<Bruker> createBrukerSet() {
        Set<Bruker> brukers = new HashSet<>();
        brukers.add(BrukerTestDataProvider.createBruker().build());
        return brukers;
    }
}