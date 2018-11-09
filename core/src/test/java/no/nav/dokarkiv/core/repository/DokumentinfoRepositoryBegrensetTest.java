package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpostWithBegrensning;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
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

    @Before
    public void setUp() {
        RequestContextUtil.createAndSetUsername("itest", "itest");
        journalpostDokumentInfoRelasjonRepository.deleteAll();
        dokumentinfoRepository.deleteAll();
        joarkRepository.deleteAll();
    }

    @Test
    public void shouldFindDokumentInfoByJournalpostIdAndDokumentInfoIdWhenNotBegrenset() {

        Journalpost journalpost = createJournalpostWithBegrensning(false);
        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentinfoRepositoryBegrenset.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId()
                .toString(), journalpost
                .getJournalpostDokumentInfoRelasjonerAlsoBegrenset()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId().toString()).isPresent(), is(true));

        assertThat(dokumentinfoRepositoryBegrenset.existsById(journalpost
                .getJournalpostDokumentInfoRelasjonerAlsoBegrenset()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()), is(true));

    }

    @Test
    public void shouldNotFindDokumentInfoByJournalpostIdAndDokumentInfoIdWhenBegrenset() {

        Journalpost journalpost = createJournalpostWithBegrensning(true);
        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentinfoRepositoryBegrenset.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId()
                .toString(), journalpost
                .getJournalpostDokumentInfoRelasjonerAlsoBegrenset()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId().toString()).isPresent(), is(false));

    }

    @Test
    public void shouldReturnFalseForExistsByIdWhenDokumentIsBegrenset() {

        Journalpost journalpost = createJournalpostWithBegrensning(true);
        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentinfoRepositoryBegrenset.existsById(journalpost
                .getJournalpostDokumentInfoRelasjonerAlsoBegrenset()
                .iterator()
                .next()
                .getDokumentInfo()
                .getId()), is(false));


    }

}