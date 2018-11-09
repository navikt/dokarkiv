package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpostWithBegrensning;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.builder.DokumentUrlInfoBuilder;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
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
import java.util.Calendar;
import java.util.UUID;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, DokumentUrlInfoRepositoryBegrenset.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@Transactional
@ActiveProfiles("itest")
public class DokumentUrlInfoRepositoryBegrensetTest {
    @Inject
    private JoarkRepository joarkRepository;

    @Inject
    private DokumentinfoRepository dokumentinfoRepository;

    @Inject
    private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;


    @Inject
    private DokumentUrlInfoRepositoryBegrenset dokumentUrlInfoRepositoryBegrenset;

    @Inject
    private DokumentUrlInfoRepository dokumentUrlInfoRepository;

    public static final String DOC_TOKEN = "token";
    public static final String FILUUID = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        RequestContextUtil.createAndSetUsername("itest", "itest");
        journalpostDokumentInfoRelasjonRepository.deleteAll();
        dokumentinfoRepository.deleteAll();
        dokumentUrlInfoRepository.deleteAll();
        joarkRepository.deleteAll();
    }


    @Test
    public void shouldNotFindByFilUUidWhenBegrenset() {

        Journalpost journalpost = createJournalpostWithBegrensning(true);
        journalpost = joarkRepository.save(journalpost);
        DokumentUrlInfo dokumentUrlInfo = DokumentUrlInfoBuilder.getDokumentUrlInfoBuilder()
                .journalpost(journalpost)
                .docToken(DOC_TOKEN)
                .filUuid(FILUUID)
                .tidspunkt(Calendar.getInstance().getTime())
                .build();
        dokumentUrlInfoRepositoryBegrenset.save(dokumentUrlInfo);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentUrlInfoRepositoryBegrenset.findByFilUuid(FILUUID), nullValue());


    }

    @Test
    public void shouldNotFindByDocTokenWhenBegrenset() {

        Journalpost journalpost = createJournalpostWithBegrensning(true);
        journalpost = joarkRepository.save(journalpost);
        DokumentUrlInfo dokumentUrlInfo = DokumentUrlInfoBuilder.getDokumentUrlInfoBuilder()
                .journalpost(journalpost)
                .docToken(DOC_TOKEN)
                .filUuid(FILUUID)
                .tidspunkt(Calendar.getInstance().getTime())
                .build();
        dokumentUrlInfoRepositoryBegrenset.save(dokumentUrlInfo);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(dokumentUrlInfoRepositoryBegrenset.findByDoctoken(DOC_TOKEN).isPresent(), is(false));


    }
}