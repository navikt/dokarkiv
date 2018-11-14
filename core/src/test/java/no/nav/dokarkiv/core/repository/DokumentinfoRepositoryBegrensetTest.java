package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.util.TestDataUtils.createBegrensning;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
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

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@Transactional
@ActiveProfiles("itest")
public class DokumentinfoRepositoryBegrensetTest {
    @Inject
    private JoarkRepository joarkRepository;

    @Inject
    private DokumentinfoRepository dokumentinfoRepository;

    @Inject
    private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

    @Inject
    private DokumentinfoRepositoryBegrenset dokumentinfoRepositoryBegrenset;

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
    public void shouldReturnNullWhenNotFound() {
        assertThat(dokumentinfoRepositoryBegrenset.findById(123L).isPresent(), is(false));
        assertThat(dokumentinfoRepositoryBegrenset.existsById(123L), is(false));
        assertThat(dokumentinfoRepositoryBegrenset.findDokumentInfoByJournalpostIdAndDokumentInfoId(123L, 123L)
                .isPresent(), is(false));
    }

    @Test
    public void shouldFindDokumentInfoByJournalpostIdAndDokumentInfoIdWhenNotBegrenset() {

        Journalpost journalpost = createJournalpost();
        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentinfoRepositoryBegrenset.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), journalpost
                .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()).isPresent(), is(true));

        assertThat(dokumentinfoRepositoryBegrenset.existsById(journalpost
                .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()), is(true));

        assertThat(dokumentinfoRepositoryBegrenset.findById(journalpost
                .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()).isPresent(), is(true));

    }

    @Test
    public void shouldNotFindDokumentInfoByJournalpostIdAndDokumentInfoIdWhenBegrenset() {

        Journalpost journalpost = createJournalpost();
        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(null, journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId(), BegrensningTypeCode.UTILGJENGELIGGJORT);
        begrensningRepository.save(begrensning);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), journalpost
                .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()).isPresent(), is(true));

        assertThat(dokumentinfoRepositoryBegrenset.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId()
                , journalpost
                        .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                        .getId()).isPresent(), is(false));

    }

    @Test
    public void shouldReturnFalseForExistsByIdWhenDokumentIsBegrenset() {

        Journalpost journalpost = createJournalpost();
        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(null, journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId(), BegrensningTypeCode.UTILGJENGELIGGJORT);
        begrensningRepository.save(begrensning);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentinfoRepository.existsById(journalpost
                .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()), is(true));

        assertThat(dokumentinfoRepositoryBegrenset.existsById(journalpost
                .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()), is(false));


    }

    @Test
    public void shouldNotFindDocumentWhenBegrenset() {

        Journalpost journalpost = createJournalpost();
        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(null, journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId(), BegrensningTypeCode.UTILGJENGELIGGJORT);
        begrensningRepository.save(begrensning);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentinfoRepository.findById(journalpost
                .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()).isPresent(), is(true));

        assertThat(dokumentinfoRepositoryBegrenset.findById(journalpost
                .getJournalpostDokumentInfoRelasjoner()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()).isPresent(), is(false));
    }
}