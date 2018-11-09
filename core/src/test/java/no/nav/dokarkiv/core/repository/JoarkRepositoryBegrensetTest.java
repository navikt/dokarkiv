package no.nav.dokarkiv.core.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.journalpostliste.TestDataUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, JoarkRepositoryBegrenset.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@Transactional
@ActiveProfiles("itest")
public class JoarkRepositoryBegrensetTest {

    @Inject
    private JoarkRepositoryBegrenset joarkRepositoryBegrenset;

    @Inject
    private JoarkRepository joarkRepository;

    @Inject
    private DokumentinfoRepository dokumentinfoRepository;

    @Inject
    private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;


    public static final String KANAL_REFERANSE_ID = "kanal";
    public static final String TILLEGGSOPPLYSNINGER_KEY = "keey";
    public static final String TILLEGGSOPPLYSNINGER_VALUE = "value";

    @Before
    public void setUp() {
        RequestContextUtil.createAndSetUsername("itest", "itest");
        journalpostDokumentInfoRelasjonRepository.deleteAll();
        dokumentinfoRepository.deleteAll();
        joarkRepository.deleteAll();
    }

    @Test
    public void shouldReturnSimilarResultAsJoarkRepositoryWhenNotBegrenset() {

        Journalpost journalpost = createJournalpost(false);
        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertTrue(joarkRepository.existsById(journalpost.getId()));
        assertTrue(joarkRepositoryBegrenset.existsById(journalpost.getId()));

        List<Journalpost> journalpostList = new ArrayList<>();
        List<Journalpost> journalpostListBegrenset = new ArrayList<>();
        joarkRepository.findAll().forEach(journalpostList::add);
        joarkRepositoryBegrenset.findAll().forEach(journalpostListBegrenset::add);

        assertThat(journalpostList.size(), is(1));
        assertThat(journalpostListBegrenset.size(), is(1));

        assertThat(joarkRepository.findById(journalpost.getId()), notNullValue());
        assertThat(joarkRepositoryBegrenset.findById(journalpost.getId()), notNullValue());

        assertThat(joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
        assertThat(joarkRepositoryBegrenset.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());

        assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
                .size(), is(1));
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
                .size(), is(1));

        assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
                .size(), is(1));
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
                .size(), is(1));

        assertTrue(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO.name())
                .isPresent());
        assertTrue(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO
                .name()).isPresent());

        assertThat(joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
        assertThat(joarkRepositoryBegrenset.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());

        assertThat(joarkRepository.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getId()
                .toString()), notNullValue());
        assertThat(joarkRepositoryBegrenset.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getId()
                .toString()), notNullValue());

        assertTrue(joarkRepository.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
        assertTrue(joarkRepositoryBegrenset.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
    }

    @Test
    public void shouldNotFindBegrensetDokument() {
        Journalpost journalpost = createJournalpost(false);
        Journalpost journalpostBegrenset = createJournalpost(true);

        journalpost = joarkRepository.save(journalpost);
        journalpostBegrenset = joarkRepository.save(journalpostBegrenset);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        List<Journalpost> journalpostList = new ArrayList<>();
        List<Journalpost> journalpostListBegrenset = new ArrayList<>();
        joarkRepository.findAll().forEach(journalpostList::add);
        joarkRepositoryBegrenset.findAll().forEach(journalpostListBegrenset::add);

        assertThat(journalpostList.size(), is(2));
        assertThat(journalpostListBegrenset.size(), is(1));
        assertThat(journalpostListBegrenset.get(0).getJournalpostId(), is(journalpost.getJournalpostId()));
    }

    @Test
    public void shouldNotExistsWhenJournalpostIsBegrenset() {
        Journalpost journalpost = createJournalpost(true);

        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertTrue(joarkRepository.existsById(journalpost.getId()));
        assertFalse(joarkRepositoryBegrenset.existsById(journalpost.getId()));
    }

    @Test
    public void shouldNotfindJournalpostIdByDokumentinfoIdWhenJournalpostIsBegrenset() {
        Journalpost journalpost = createJournalpost(true);
        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(joarkRepository.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getId()
                .toString()), notNullValue());
        assertThat(joarkRepositoryBegrenset.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getId()
                .toString()), nullValue());
    }

    @Test
    public void shouldNotfindDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdiWhenJournalpostIsBegrenset() {
        Journalpost journalpost = createJournalpost(true);

        joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
        assertThat(joarkRepositoryBegrenset.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), nullValue());
    }

    @Test
    public void shouldNotfindJournalpostIdByTilleggsopplysningerNokkelAndVerdiWhenJournalpostIsBegrenset() {

        Journalpost journalpost = createJournalpost(true);

        joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
        assertThat(joarkRepositoryBegrenset.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), nullValue());
    }

    @Test
    public void shouldNotfindJournalpostIdByKanalReferanseIdAndMottakskanalWhenJournalpostIsBegrenset() {
        Journalpost journalpost = createJournalpost(true);

        joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
                .size(), is(1));
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
                .size(), is(0));

        assertTrue(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO.name())
                .isPresent());
        assertFalse(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO
                .name()).isPresent());
    }

    @Test
    public void shouldNotfindJournalpostByKanalReferanseIdWhenJournalpostIsBegrenset() {

        Journalpost journalpost = createJournalpost(true);

        joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertTrue(joarkRepository.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
        assertFalse(joarkRepositoryBegrenset.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
    }


    private Journalpost createJournalpost(boolean withBegrensning) {
        Journalpost journalpost = TestDataUtils.createJournalpost().build();

        Map<String, String> map = new HashMap<>();
        map.put(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE);
        journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setTilleggsopplysninger(map);
        journalpost.setTilleggsopplysninger(map);

        journalpost.setKanalReferanseId(KANAL_REFERANSE_ID);
        journalpost.setMottakskanal(MottaksKanalCode.NAV_NO);

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