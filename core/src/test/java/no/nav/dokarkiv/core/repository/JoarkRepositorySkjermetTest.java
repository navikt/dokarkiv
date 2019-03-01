package no.nav.dokarkiv.core.repository;

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
public class JoarkRepositorySkjermetTest {

	@Inject
	private JoarkRepositorySkjermet joarkRepositorySkjermet;

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private DokumentinfoRepository dokumentinfoRepository;

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	private SkjermingService skjermingService;

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
		entityManager.createNativeQuery("Delete from t_jp_tillegg").getFirstResult();

	}


	@Test
	public void shouldReturnNullOrFalseWhenNotFound() {
		assertThat(joarkRepositorySkjermet.findById(123L).isPresent(), is(false));
		assertThat(joarkRepositorySkjermet.existsById(123L), is(false));
		assertThat(joarkRepositorySkjermet.findAll().spliterator().estimateSize(), is(0L));
		assertThat(joarkRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal("test", "test")
				.isPresent(), is(false));
		assertThat(joarkRepositorySkjermet.findJournalpostByKanalReferanseId("test").isPresent(), is(false));
		assertThat(joarkRepositorySkjermet.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi("test", "test"), nullValue());
		assertThat(joarkRepositorySkjermet.findJournalpostIdByDokumentinfoId("213"), nullValue());
		assertThat(joarkRepositorySkjermet.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi("213", "313"), nullValue());
		assertThat(joarkRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal("213", MottaksKanalCode.NAV_NO)
				.size(), is(0));
	}


	@Test
	public void shouldReturnSameResultAsJoarkRepositoryWhenNotSkjermet() {

		Journalpost journalpost = createJournalpost();
		journalpost = joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();

		assertTrue(joarkRepository.existsById(journalpost.getId()));
		assertTrue(joarkRepositorySkjermet.existsById(journalpost.getId()));

		List<Journalpost> journalpostList = new ArrayList<>();
		List<Journalpost> journalpostListSkjermet = new ArrayList<>();
		joarkRepository.findAll().forEach(journalpostList::add);
		joarkRepositorySkjermet.findAll().forEach(journalpostListSkjermet::add);

		assertThat(journalpostList.size(), is(1));
		assertThat(journalpostListSkjermet.size(), is(1));

		assertThat(joarkRepository.findById(journalpost.getId()).isPresent(), is(true));
		assertThat(joarkRepositorySkjermet.findById(journalpost.getId()).isPresent(), is(true));

		assertThat(joarkRepository.findAllJournalpostIdsByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));
		assertThat(joarkRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		assertThat(joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
		assertThat(joarkRepositorySkjermet.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());

		assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));
		assertThat(joarkRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));

		assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));
		assertThat(joarkRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));

		assertTrue(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO.name())
				.isPresent());
		assertTrue(joarkRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO
				.name()).isPresent());

		assertThat(joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
		assertThat(joarkRepositorySkjermet.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());

		assertThat(joarkRepository.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), notNullValue());
		assertThat(joarkRepositorySkjermet.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), notNullValue());

		assertTrue(joarkRepository.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
		assertTrue(joarkRepositorySkjermet.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
	}

	@Test
	public void shouldNotReturnSkjermetJournalpostForFindById() {
		Journalpost journalpost = createJournalpost();


		journalpost = joarkRepository.save(journalpost);

		skjermingService.setJournalpostSkjermet(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThat(joarkRepository.findById(journalpost.getId()).isPresent(), is(true));
		assertThat(joarkRepositorySkjermet.findById(journalpost.getId()).isPresent(), is(false));

	}

	@Test
	public void shouldNotFindSkjermetJournalpost() {
		Journalpost journalpost = createJournalpost();
		Journalpost journalpostSkjermet = createJournalpost();

		journalpost = joarkRepository.save(journalpost);
		journalpostSkjermet = joarkRepository.save(journalpostSkjermet);
		skjermingService.setJournalpostSkjermet(journalpostSkjermet, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		List<Journalpost> journalpostList = new ArrayList<>();
		List<Journalpost> journalpostListSkjermet = new ArrayList<>();
		joarkRepository.findAll().forEach(journalpostList::add);
		joarkRepositorySkjermet.findAll().forEach(journalpostListSkjermet::add);

		assertThat(journalpostList.size(), is(2));
		assertThat(journalpostListSkjermet.size(), is(1));
		assertThat(journalpostListSkjermet.get(0).getJournalpostId(), is(journalpost.getJournalpostId()));
	}

	@Test
	public void shouldNotExistsWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpost = joarkRepository.save(journalpost);

		skjermingService.setJournalpostSkjermet(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertTrue(joarkRepository.existsById(journalpost.getId()));
		assertFalse(joarkRepositorySkjermet.existsById(journalpost.getId()));
	}

	@Test
	public void shouldNotfindJournalpostIdByDokumentinfoIdWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpost = joarkRepository.save(journalpost);
		skjermingService.setJournalpostSkjermet(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(joarkRepository.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), notNullValue());
		assertThat(joarkRepositorySkjermet.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), nullValue());
	}

	@Test
	public void shouldNotfindJournalpostIdByTilleggsopplysningerNokkelAndVerdiWhenJournalpostIsSkjermet() {

		Journalpost journalpost = createJournalpost();

		joarkRepository.save(journalpost);
		skjermingService.setJournalpostSkjermet(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), is(journalpost
				.getJournalpostId()));
		assertThat(joarkRepositorySkjermet.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), nullValue());
	}

	@Test
	public void shouldNotfindJournalpostIdByKanalReferanseIdAndMottakskanalWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpost = joarkRepository.save(journalpost);
		skjermingService.setJournalpostSkjermet(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));
		assertThat(joarkRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(0));

		assertThat(joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO.name())
				.isPresent(), is(true));
		assertThat(joarkRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO
				.name()).isPresent(), is(false));
	}

	@Test
	public void shouldNotfindJournalpostByKanalReferanseIdWhenJournalpostIsSkjermet() {

		Journalpost journalpost = createJournalpost();

		joarkRepository.save(journalpost);
		skjermingService.setJournalpostSkjermet(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(joarkRepository.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent(), is(true));
		assertThat(joarkRepositorySkjermet.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent(), is(false));
	}


	@Test
	public void shouldNotReturnSkjermetJournalpostIdForFindAllJournalpostIdsByDokumentInfoId() {
		Journalpost journalpost = createJournalpost();
		joarkRepository.save(journalpost);

		Journalpost journalpostSkjermet = createJournalpost();

		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		//Legg til dokumentInfo som vedlegg til skjermet journalpost
		journalpostSkjermet.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.opprettetKildeNavn("test")
				.tilknyttetAvNavn("test")
				.dokumentInfo(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
				.build());

		journalpostSkjermet = joarkRepository.save(journalpostSkjermet);
		skjermingService.setJournalpostSkjermet(journalpostSkjermet, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThat(joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId).size(), is(2));
		assertThat(joarkRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId).size(), is(1));
		assertThat(joarkRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId)
				.get(0), is(journalpost.getJournalpostId()));

	}
}