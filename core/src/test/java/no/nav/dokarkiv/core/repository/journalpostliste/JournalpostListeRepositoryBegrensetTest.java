package no.nav.dokarkiv.core.repository.journalpostliste;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepositoryBegrenset;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataUtils;
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
import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, JournalpostListeRepository.class})
@DataJpaTest
@Transactional
@ActiveProfiles("itest")
public class JournalpostListeRepositoryBegrensetTest {

    @Inject
    private JoarkRepository joarkRepository;

    @Inject
    private DokumentinfoRepository dokumentinfoRepository;

    @Inject
    private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

    @Inject
    private DokumentinfoRepositoryBegrenset dokumentinfoRepositoryBegrenset;

    @Inject
    private JournalpostListeRepository journalpostListeRepository;

    @Before
    public void setUp() {
        RequestContextUtil.createAndSetUsername("itest", "itest");
        journalpostDokumentInfoRelasjonRepository.deleteAll();
        dokumentinfoRepository.deleteAll();
        joarkRepository.deleteAll();
    }

    @Test
    public void shouldNotCountJournalpostWhenBegrenset() {
        Journalpost journalpostBegrenset = createJournalpost(true);
        Journalpost journalpost = createJournalpost(false);
        journalpost = joarkRepository.save(journalpost);
        journalpostBegrenset = joarkRepository.save(journalpostBegrenset);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HentMinJPListeParameters hentMinJPListeParameters = new HentMinJPListeParameters();
        hentMinJPListeParameters.setSaksListe(Arrays.asList(new SakFagsystem(FagsystemCode.PEN, "123")));

        Long totalNumberOfJournalpostsBegrenset = journalpostListeRepository.findTotalNumberOfJournalposts(hentMinJPListeParameters);


        hentMinJPListeParameters.setWithBegrensetJP(true);
        Long totalNumberOfJournalpostsAll = journalpostListeRepository.findTotalNumberOfJournalposts(hentMinJPListeParameters);

        assertThat(totalNumberOfJournalpostsBegrenset, is(1L));
        assertThat(totalNumberOfJournalpostsAll, is(2L));
    }

    @Test
    public void shouldNotGetJournalpostWhenBegrenset() {
        Journalpost journalpostBegrenset = createJournalpost(true);
        Journalpost journalpost = createJournalpost(false);
        journalpost = joarkRepository.save(journalpost);
        journalpostBegrenset = joarkRepository.save(journalpostBegrenset);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HentMinJPListeParameters hentMinJPListeParameters = new HentMinJPListeParameters();
        hentMinJPListeParameters.setSaksListe(Arrays.asList(new SakFagsystem(FagsystemCode.PEN, "123")));

        List<Journalpost> journalpostListBegrenset = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);


        hentMinJPListeParameters.setWithBegrensetJP(true);
        List<Journalpost> journalpostListBegrensetAll = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);

        assertThat(journalpostListBegrenset.size(), is(1));
        assertThat(journalpostListBegrensetAll.size(), is(2));
    }


    private Journalpost createJournalpost(boolean withBegrensning) {
        Journalpost journalpost = TestDataUtils.createJournalpost().build();

        if (withBegrensning) {
            Begrensning begrensning = Begrensning.builder()
                    .begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
                    .journalpost(journalpost)
                    .build();
            begrensning.setOpprettetKildeNavn("Kilde navn");
            journalpost.addBegrensning(begrensning);
        }

        return journalpost;
    }

}