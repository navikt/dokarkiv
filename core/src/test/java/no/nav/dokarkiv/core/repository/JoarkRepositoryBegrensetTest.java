package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.util.TestDataUtils.createBegrensning;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, SkjermingService.class, JdbcAbacSecurityRepository.class})
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

    @Inject
    private BegrensningRepository begrensningRepository;

    @Inject
    private EntityManager entityManager;

    public static final String KANAL_REFERANSE_ID = "kanal";
    public static final String TILLEGGSOPPLYSNINGER_KEY = "keey";
    public static final String TILLEGGSOPPLYSNINGER_VALUE = "value";

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
        entityManager.createNativeQuery("Delete from t_jp_tillegg").getFirstResult();

    }


    @Test
    public void shouldReturnNullOrFalseWhenNotFound() {
        assertThat(joarkRepositoryBegrenset.findById(123L).isPresent(), is(false));
        assertThat(joarkRepositoryBegrenset.existsById(123L), is(false));
        assertThat(joarkRepositoryBegrenset.findAll().spliterator().estimateSize(), is(0L));
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal("test", "test")
                .isPresent(), is(false));
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseId("test").isPresent(), is(false));
        assertThat(joarkRepositoryBegrenset.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi("test", "test"), nullValue());
        assertThat(joarkRepositoryBegrenset.findJournalpostIdByDokumentinfoId("213"), nullValue());
        assertThat(joarkRepositoryBegrenset.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi("213", "313"), nullValue());
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal("213", MottaksKanalCode.NAV_NO)
                .size(), is(0));
    }


    @Test
    public void shouldReturnSameResultAsJoarkRepositoryWhenNotBegrenset() {

        Journalpost journalpost = createJournalpost();
        journalpost = joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();

        assertTrue(joarkRepository.existsById(journalpost.getId()));
        assertTrue(joarkRepositoryBegrenset.existsById(journalpost.getId()));

        List<Journalpost> journalpostList = new ArrayList<>();
        List<Journalpost> journalpostListBegrenset = new ArrayList<>();
        joarkRepository.findAll().forEach(journalpostList::add);
        joarkRepositoryBegrenset.findAll().forEach(journalpostListBegrenset::add);

        assertThat(journalpostList.size(), is(1));
        assertThat(journalpostListBegrenset.size(), is(1));

        assertThat(joarkRepository.findById(journalpost.getId()).isPresent(), is(true));
        assertThat(joarkRepositoryBegrenset.findById(journalpost.getId()).isPresent(), is(true));

        assertThat(joarkRepository.findAllJournalpostIdsByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId()).size(), is(1));
        assertThat(joarkRepositoryBegrenset.findAllJournalpostIdsByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId()).size(), is(1));

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
    public void shouldReturnBegrensetJournalpostForFindById() {
        Journalpost journalpost = createJournalpost();

        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), null, SkjermingTypeCode.POL);

        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();

        assertThat(joarkRepository.findById(journalpost.getId()).isPresent(), is(true));
        assertThat(joarkRepositoryBegrenset.findById(journalpost.getId()).isPresent(), is(false));

    }

    @Test
    public void shouldNotFindBegrensetDokument() {
        Journalpost journalpost = createJournalpost();
        Journalpost journalpostBegrenset = createJournalpost();

        journalpost = joarkRepository.save(journalpost);
        journalpostBegrenset = joarkRepository.save(journalpostBegrenset);
        Begrensning begrensning = createBegrensning(journalpostBegrenset.getJournalpostId(), null, SkjermingTypeCode.POL);
        begrensningRepository.save(begrensning);
        TestTransaction.flagForCommit();

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
        Journalpost journalpost = createJournalpost();

        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), null, SkjermingTypeCode.POL);

        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();

        assertTrue(joarkRepository.existsById(journalpost.getId()));
        assertFalse(joarkRepositoryBegrenset.existsById(journalpost.getId()));
    }

    @Test
    public void shouldNotfindJournalpostIdByDokumentinfoIdWhenJournalpostIsBegrenset() {
        Journalpost journalpost = createJournalpost();

        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), null, SkjermingTypeCode.POL);
        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();

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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldNotfindDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdiWhenJournalpostIsBegrenset() {
        Journalpost journalpost = createJournalpost();

        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), null, SkjermingTypeCode.POL);
        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();

        assertThat(joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), is(journalpost
                .getJournalpostId()));
        assertThat(joarkRepositoryBegrenset.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), nullValue());

    }

    @Test
    public void shouldNotfindJournalpostIdByTilleggsopplysningerNokkelAndVerdiWhenJournalpostIsBegrenset() {

        Journalpost journalpost = createJournalpost();

        joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), null, SkjermingTypeCode.POL);
        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();

        assertThat(joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), is(journalpost
                .getJournalpostId()));
        assertThat(joarkRepositoryBegrenset.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), nullValue());
    }

    @Test
    public void shouldNotfindJournalpostIdByKanalReferanseIdAndMottakskanalWhenJournalpostIsBegrenset() {
        Journalpost journalpost = createJournalpost();

        journalpost = joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), null, SkjermingTypeCode.POL);
        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();

        assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
                .size(), is(1));
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
                .size(), is(0));

        assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO.name())
                .isPresent(), is(true));
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO
                .name()).isPresent(), is(false));
    }

    @Test
    public void shouldNotfindJournalpostByKanalReferanseIdWhenJournalpostIsBegrenset() {

        Journalpost journalpost = createJournalpost();

        joarkRepository.save(journalpost);
        Begrensning begrensning = createBegrensning(journalpost.getJournalpostId(), null, SkjermingTypeCode.POL);

        begrensningRepository.save(begrensning);

        TestTransaction.flagForCommit();

        assertThat(joarkRepository.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent(), is(true));
        assertThat(joarkRepositoryBegrenset.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent(), is(false));
    }


    @Test
    public void shouldReturnBegrensetJournalpostIdForFindAllJournalpostIdsByDokumentInfoId() {
        Journalpost journalpost = createJournalpost();
        joarkRepository.save(journalpost);

        Journalpost journalpostBegrenset = createJournalpost();

        Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        //Legg til dokumentInfo som vedlegg til begrenset journalpost
        journalpostBegrenset.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .opprettetKildeNavn("test")
                .tilknyttetAvNavn("test")
                .dokumentInfo(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
                .build());

        journalpostBegrenset = joarkRepository.save(journalpostBegrenset);
        Begrensning begrensning = createBegrensning(journalpostBegrenset.getJournalpostId(), null, SkjermingTypeCode.POL);
        begrensningRepository.save(begrensning);
        TestTransaction.flagForCommit();


        assertThat(joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId).size(), is(2));
        assertThat(joarkRepositoryBegrenset.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId).size(), is(1));
        assertThat(joarkRepositoryBegrenset.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId)
                .get(0), is(journalpost.getJournalpostId()));

    }


}