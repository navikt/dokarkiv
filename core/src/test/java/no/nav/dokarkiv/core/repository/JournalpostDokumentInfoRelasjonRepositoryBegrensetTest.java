package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.repository.journalpostliste.TestDataUtils.createJournalpost;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
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


    @Before
    public void setUp() {
        RequestContextUtil.createAndSetUsername("itest", "itest");
    }

    @Test
    public void shouldNotReturnJournalpostDokumentRelasjonWhereJournalpostIsBegrenset() {
        Journalpost journalpost = createJournalpost().build();
        Begrensning begrensning = Begrensning.builder()
                .begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
                .journalpost(journalpost)
                .build();
        begrensning.setOpprettetKildeNavn("taaaaaaaaaaaaaaaa");
        journalpost.addBegrensning(begrensning);

        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        Optional<List<JournalpostDokumentInfoRelasjon>> journalpostDokumentInfoRelasjonsBegrenset = journalpostDokumentInfoRelasjonRepositoryBegrenset
                .findByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                        .getDokumentInfo()
                        .getDokumentInfoId());

        Optional<List<JournalpostDokumentInfoRelasjon>> journalpostDokumentInfoRelasjons = journalpostDokumentInfoRelasjonRepository
                .findByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                        .getDokumentInfo()
                        .getDokumentInfoId());

        assertThat(journalpostDokumentInfoRelasjons.get().size(), is(1));
        assertThat(journalpostDokumentInfoRelasjonsBegrenset.get().size(), is(0));
    }
}