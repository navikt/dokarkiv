package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.util.TestDataUtils.createBegrensning;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.CoreMatchers.is;
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
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, JournalpostDokumentInfoRelasjonRepositoryBegrenset.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@Transactional
@ActiveProfiles("itest")
public class JournalpostDokumentInfoRelasjonRepositoryBegrensetTest {

    @Inject
    private JournalpostDokumentInfoRelasjonRepositoryBegrenset journalpostDokumentInfoRelasjonRepositoryBegrenset;

    @Inject
    private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

    @Inject
    private JoarkRepository joarkRepository;

    @Inject
    private DokumentinfoRepository dokumentinfoRepository;

    @Inject
    private BegrensningRepository begrensningRepository;

    @Before
    public void setUp() {
        RequestContextUtil.createAndSetUsername("itest", "itest");
    }

    @After
    public void cleanUp() {
        journalpostDokumentInfoRelasjonRepository.deleteAll();
        dokumentinfoRepository.deleteAll();
        joarkRepository.deleteAll();
        begrensningRepository.deleteAll();
    }


    @Test
    public void shouldReturnJournalpostDokumentRelasjonWhenNotBegrenset() {
        Journalpost journalpost = createJournalpost();
        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(journalpostDokumentInfoRelasjonRepositoryBegrenset.findAllByDokumentInfoDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId()).get().size(), is(1));
        assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId()).get().size(), is(1));
    }

    @Test
    public void shouldNotReturnJournalpostDokumentRelasjonWhereJournalpostIsBegrenset() {
        Journalpost journalpost = createAndSaveJournalpostWithTwoRelasjonerWhereOneIsBegrenset();

        Optional<List<JournalpostDokumentInfoRelasjon>> journalpostDokumentInfoRelasjonsBegrenset = journalpostDokumentInfoRelasjonRepositoryBegrenset
                .findAllByDokumentInfoDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                        .getDokumentInfo()
                        .getDokumentInfoId());

        Optional<List<JournalpostDokumentInfoRelasjon>> journalpostDokumentInfoRelasjons = journalpostDokumentInfoRelasjonRepository
                .findAllByDokumentInfoDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                        .getDokumentInfo()
                        .getDokumentInfoId());

        assertThat(journalpostDokumentInfoRelasjons.get().size(), is(1));
        assertThat(journalpostDokumentInfoRelasjonsBegrenset.get().size(), is(0));
    }

    @Test
    public void shouldGetAllBegrensetJournalpostIdsByDokumentInfoId() {
        Journalpost journalpost = createAndSaveJournalpostWithTwoRelasjonerWhereOneIsBegrenset();
        Long hoveddokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        List<Long> journalpostIds = journalpostDokumentInfoRelasjonRepository.findBegrensetRelasjonJournalpostIdByDokumentInfoId(hoveddokumentInfoId)
                .orElse(new ArrayList());

        assertThat(journalpostIds.size(), is(1));
        assertThat(String.valueOf(journalpostIds.get(0)), is(String.valueOf(journalpost.getJournalpostId())));
    }

    @Test
    public void shouldGetAllBegrensetDokumentInfoIdsByJournalpostId() {
        Journalpost journalpost = createAndSaveJournalpostWithTwoRelasjonerWhereOneIsBegrenset();
        Long hoveddokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        List<Long> dokumentInfoIds = journalpostDokumentInfoRelasjonRepository.findBegrensetRelasjonDokumentInfoIdByJournalpostId(journalpost
                .getJournalpostId()).orElse(new ArrayList());

        assertThat(dokumentInfoIds.size(), is(1));
        assertThat(String.valueOf(dokumentInfoIds.get(0)), is(String.valueOf(hoveddokumentInfoId)));
    }

    private Journalpost createAndSaveJournalpostWithTwoRelasjonerWhereOneIsBegrenset() {
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


        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId(), BegrensningTypeCode.UTILGJENGELIGGJORT);
        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        return journalpost;
    }
}