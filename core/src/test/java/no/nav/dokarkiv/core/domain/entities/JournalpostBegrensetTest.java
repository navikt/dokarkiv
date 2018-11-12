package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

/**
 * Unit tests for Journalpost.
 *
 * @author Per Kristian Foss, Visma Sirius
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalpostBegrensetTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private Journalpost journalpost;

    private static final Long DOKUMENTINFOID_BEGRENSET = 1L;
    private static final Long DOKUMENTINFOID_NOT_BEGRENSET = 2L;
    private static final Long JOURNALPOST_ID = 1L;

    @Test
    public void shouldNotGetRelasjonWithBegrensetDokumentInfo() {

        journalpost = createJournalpostWithOneBegrensetDokumentInfoRelasjon();
        Set<JournalpostDokumentInfoRelasjon> relasjonsBegrenset = journalpost.getJournalpostDokumentInfoRelasjoner();
        Set<JournalpostDokumentInfoRelasjon> relasjons = journalpost.getJournalpostDokumentInfoRelasjonerAlsoBegrenset();

        assertThat(relasjonsBegrenset.size(), is(1));
        assertThat(relasjonsBegrenset.iterator().next().getTilknyttetJournalpostSom(), is(VEDLEGG));

        assertThat(relasjons.size(), is(2));
    }

    @Test
    public void shouldNotGetRelasjonWithBegrensetDokumentInfo_getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId() {
        journalpost = createJournalpostWithOneBegrensetDokumentInfoRelasjon();

        assertThat(journalpost.getJournalpostDokumentInfoRelasjonerAlsoBegrenset().size(), is(2));
        assertThat(journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENTINFOID_BEGRENSET), nullValue());
        assertThat(journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENTINFOID_NOT_BEGRENSET), notNullValue());
    }

    @Test
    public void shouldNotGetRelasjonWithBegrensetDokumentInfo_verifyFieldsForEndeligJournalforing() {
        expectedException.expect(InvalidArgumentException.class);
        expectedException.expectMessage("Journalpost must have at least one DokumentInfoRelasjon");
        journalpost = createJournalpostWithOneBegrensetDokumentInfoRelasjon();

        journalpost.removeJournalpostDokumentInfoRelasjon(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next());
        journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
                .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
                        .dokumentInfoId(DOKUMENTINFOID_BEGRENSET)
                        .begrensning(Begrensning.builder()
                                .begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
                                .build())
                        .build())
                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT).build());

        assertThat(journalpost.getJournalpostDokumentInfoRelasjonerAlsoBegrenset().size(), is(2));
        journalpost.verifyMandatoryFields();
    }

    private Journalpost createJournalpostWithOneBegrensetDokumentInfoRelasjon() {
        return getJournalpostBuilder()
                .journalpostId(JOURNALPOST_ID)
                .journalStatus(JournalStatusCode.J)
                .journalpostType(JournalpostTypeCode.I)
                .endretAvNavn("HHA")
                .fagomrade(FagomradeCode.PEN)
                .innhold("ASDasd")
                .avsenderMottaker("asdasd")
                .saksrelasjon(Saksrelasjon.builder().build())
                .brukere(Bruker.builder().build())
                .journalForendeEnhetId("qsda")
                .dokumentInfoRelasjoner(
                        getJournalpostDokumentInfoRelasjonBuilder()
                                .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
                                        .dokumentInfoId(DOKUMENTINFOID_BEGRENSET)
                                        .begrensning(Begrensning.builder()
                                                .begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
                                                .build())
                                        .build())
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT).build(),
                        getJournalpostDokumentInfoRelasjonBuilder()
                                .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
                                        .dokumentInfoId(DOKUMENTINFOID_NOT_BEGRENSET)
                                        .build())
                                .tilknyttetJournalpostSom(VEDLEGG).build()).build();
    }


}
