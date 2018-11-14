package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.util.TestDataUtils.createBegrensning;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@ActiveProfiles("itest")
public class JournalpostBegrensetTest {

    @Inject
    private JoarkRepositoryBegrenset joarkRepositoryBegrenset;

    @Inject
    private JoarkRepository joarkRepository;

    @Inject
    private DokumentinfoRepository dokumentinfoRepository;

    @Inject
    private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

    @Inject
    private BegrensningRepository begrensningRepository;


    @Before
    public void setUp() {
        RequestContextUtil.createAndSetUsername("itest", "itest");
    }

    @After
    public void cleanUp() {
        TestTransaction.end();
        journalpostDokumentInfoRelasjonRepository.deleteAll();
        dokumentinfoRepository.deleteAll();
        joarkRepository.deleteAll();
        begrensningRepository.deleteAll();
    }

    @Test
    public void shouldNotReturnBegrensetJournalpostDokumentInfoRelasjons() {

        Journalpost journalpost = createJournalpostWithTwoVedlegg();

        journalpost = joarkRepository.save(journalpost);
        JournalpostDokumentInfoRelasjon begrensetJournalpostDokumentInfoRelasjon = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .iterator()
                .next();
        Long begrensetDokumentInfoId = begrensetJournalpostDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId();
        Long begrensetDokumentFildetaljId = journalpost.findDokumentInfoById(begrensetDokumentInfoId)
                .getFildetaljerListe()
                .iterator()
                .next()
                .getFildetaljerId();

        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), begrensetDokumentInfoId, BegrensningTypeCode.UTILGJENGELIGGJORT);
        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();

        //Test behaviour when begrenset
        Journalpost journalpostWithBegrensning = joarkRepositoryBegrenset.findById(journalpost.getJournalpostId()).get();

        assertThat(journalpostWithBegrensning.getJournalpostDokumentInfoRelasjoner().size(), is(2));
        assertThat(journalpostWithBegrensning.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .anyMatch(rel -> rel.getDokumentInfo().getDokumentInfoId().equals(begrensetDokumentInfoId)), is(false));
        assertThat(journalpostWithBegrensning.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(begrensetDokumentInfoId), nullValue());
        assertThat(journalpostWithBegrensning.findDokumentInfoById(begrensetDokumentInfoId), nullValue());
        assertThat(journalpostWithBegrensning.getBegrensetRelasjonerDokumentInfoId().size(), is(1));
        assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .size(), is(1));
        assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .stream()
                .anyMatch(rel -> rel.getDokumentInfo().getDokumentInfoId().equals(begrensetDokumentInfoId)), is(false));
        assertThat(journalpostWithBegrensning.findAllFilDetaljer().size(), is(2));
        assertThat(journalpostWithBegrensning.findAllFilDetaljer()
                .stream()
                .anyMatch(detalj -> detalj.getDokumentInfo().getDokumentInfoId().equals(begrensetDokumentInfoId)), is(false));
        assertThat(journalpostWithBegrensning.findFilDetaljerByFilDetaljerId(begrensetDokumentFildetaljId), nullValue());
        assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonById(begrensetJournalpostDokumentInfoRelasjon.getJournalpostDokumentInfoRelasjonId()), nullValue());
        assertThat(journalpostWithBegrensning.findAllDokumentInfos().size(), is(2));

        //Test behaviour when not begrenset
        journalpostWithBegrensning.addAllbegrensetRelasjonDokumentInfoIds(new ArrayList<>());
        Journalpost journalpostWithoutBegrensning = joarkRepository.findById(journalpost.getJournalpostId()).get();

        assertThat(journalpostWithoutBegrensning.getJournalpostDokumentInfoRelasjoner().size(), is(3));
        assertThat(journalpostWithoutBegrensning.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .anyMatch(rel -> rel.getDokumentInfo().getDokumentInfoId().equals(begrensetDokumentInfoId)), is(true));
        assertThat(journalpostWithoutBegrensning.getBegrensetRelasjonerDokumentInfoId().size(), is(0));
        assertThat(journalpostWithBegrensning.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(begrensetDokumentInfoId), notNullValue());
        assertThat(journalpostWithBegrensning.findDokumentInfoById(begrensetDokumentInfoId), notNullValue());
        assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .size(), is(2));
        assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .stream()
                .anyMatch(rel -> rel.getDokumentInfo().getDokumentInfoId().equals(begrensetDokumentInfoId)), is(true));
        assertThat(journalpostWithBegrensning.findAllFilDetaljer().size(), is(3));
        assertThat(journalpostWithBegrensning.findAllFilDetaljer()
                .stream()
                .anyMatch(detalj -> detalj.getDokumentInfo().getDokumentInfoId().equals(begrensetDokumentInfoId)), is(true));
        assertThat(journalpostWithBegrensning.findFilDetaljerByFilDetaljerId(begrensetDokumentFildetaljId), notNullValue());
        assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonById(begrensetJournalpostDokumentInfoRelasjon.getJournalpostDokumentInfoRelasjonId()), notNullValue());
        assertThat(journalpostWithBegrensning.findAllDokumentInfos().size(), is(3));

    }

    private Journalpost createJournalpostWithTwoVedlegg() {
        Journalpost journalpost = createJournalpost();

        journalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .opprettetKildeNavn("test")
                .tilknyttetAvNavn("test")
                .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
                        .dokumentstatus(DokumentStatusCode.FERDIGSTILT)
                        .opprettetKildeNavn("test")
                        .filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
                                .filtype(FilTypeCode.PDF)
                                .filUuid("uuid")
                                .variantFormat(VariantFormatCode.PRODUKSJON)
                                .opprettetKildeNavn("test")
                                .build()
                        )
                        .build()).build());

        journalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .opprettetKildeNavn("test")
                .tilknyttetAvNavn("test")
                .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
                        .dokumentstatus(DokumentStatusCode.FERDIGSTILT)
                        .opprettetKildeNavn("test")
                        .filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
                                .filtype(FilTypeCode.PDF)
                                .filUuid("uuid")
                                .variantFormat(VariantFormatCode.PRODUKSJON)
                                .opprettetKildeNavn("test")
                                .build()
                        )
                        .build()).build());


        return journalpost;
    }

}
