package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.dokarkiv.core.util.TestDataUtils.KANAL_REFERANSE_ID;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, JdbcAbacSecurityRepository.class})
@Transactional
@ActiveProfiles("itest")
public class JournalpostRepositorySkjermetTest {

	@Autowired
	private JournalpostRepositorySkjermet journalpostRepositorySkjermet;

	@Autowired
	private JournalpostRepository journalpostRepository;

	@Autowired
	private DokumentInfoTestRepository dokumentInfoTestRepository;

	@Autowired
	private JournalpostDokumentInfoRelasjonTestRepository journalpostDokumentInfoRelasjonTestRepository;

	@Autowired
	private SkjermingService skjermingService;

	@Autowired
	private EntityManager entityManager;

	public static final String TILLEGGSOPPLYSNINGER_KEY = "keey";
	public static final String TILLEGGSOPPLYSNINGER_VALUE = "value";

	@BeforeEach
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@AfterEach
	public void cleanUp() {
		TestTransaction.end();
		journalpostDokumentInfoRelasjonTestRepository.deleteAll();
		dokumentInfoTestRepository.deleteAll();
		journalpostRepository.deleteAll();
		entityManager.createNativeQuery("Delete from t_jp_tillegg").getFirstResult();
	}

	@Test
	public void shouldReturnNullOrFalseWhenNotFound() {
		assertThat(journalpostRepositorySkjermet.findById(123L).isPresent(), is(false));
		assertThat(journalpostRepositorySkjermet.existsById(123L), is(false));
		assertThat(journalpostRepositorySkjermet.findAll().spliterator().estimateSize(), is(0L));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal("test", "test")
				.isPresent(), is(false));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseId("test").isPresent(), is(false));
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi("test", "test"), nullValue());
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByDokumentinfoId("213"), nullValue());
		assertThat(journalpostRepositorySkjermet.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi("213", "313"), nullValue());
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal("213", MottaksKanalCode.NAV_NO)
				.size(), is(0));
	}

	@Test
	public void shouldReturnSameResultAsRepositoryWhenNotSkjermet() {

		Journalpost journalpost = createJournalpost();
		journalpost = journalpostRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertTrue(journalpostRepository.existsById(journalpost.getId()));
		assertTrue(journalpostRepositorySkjermet.existsById(journalpost.getId()));

		List<Journalpost> journalpostList = new ArrayList<>();
		List<Journalpost> journalpostListSkjermet = new ArrayList<>();
		journalpostRepository.findAll().forEach(journalpostList::add);
		journalpostRepositorySkjermet.findAll().forEach(journalpostListSkjermet::add);

		assertThat(journalpostList.size(), is(1));
		assertThat(journalpostListSkjermet.size(), is(1));

		assertThat(journalpostRepository.findById(journalpost.getId()).isPresent(), is(true));
		assertThat(journalpostRepositorySkjermet.findById(journalpost.getId()).isPresent(), is(true));

		assertThat(journalpostRepository.findAllJournalpostIdsByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));
		assertThat(journalpostRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		assertThat(journalpostRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());

		assertThat(journalpostRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));

		assertThat(journalpostRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));

		assertTrue(journalpostRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO.name())
				.isPresent());
		assertTrue(journalpostRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO
				.name()).isPresent());

		assertThat(journalpostRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
		assertThat(journalpostRepositorySkjermet.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());

		assertThat(journalpostRepository.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), notNullValue());
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), notNullValue());

		assertTrue(journalpostRepository.findTopByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
		assertTrue(journalpostRepositorySkjermet.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
	}

	@Test
	public void shouldNotReturnSkjermetJournalpostForFindById() {
		Journalpost journalpost = createJournalpost();


		journalpost = journalpostRepository.save(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThat(journalpostRepository.findById(journalpost.getId()).isPresent(), is(true));
		assertThat(journalpostRepositorySkjermet.findById(journalpost.getId()).isPresent(), is(false));

	}

	@Test
	public void shouldNotFindSkjermetJournalpost() {
		Journalpost journalpost = createUniqueJournalpost();
		Journalpost journalpostSkjermet = createUniqueJournalpost();

		journalpost = journalpostRepository.save(journalpost);
		journalpostSkjermet = journalpostRepository.save(journalpostSkjermet);
		skjermingService.setJournalpostSkjerming(journalpostSkjermet.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		List<Journalpost> journalpostList = new ArrayList<>();
		List<Journalpost> journalpostListSkjermet = new ArrayList<>();
		journalpostRepository.findAll().forEach(journalpostList::add);
		journalpostRepositorySkjermet.findAll().forEach(journalpostListSkjermet::add);

		assertThat(journalpostList.size(), is(2));
		assertThat(journalpostListSkjermet.size(), is(1));
		assertThat(journalpostListSkjermet.get(0).getJournalpostId(), is(journalpost.getJournalpostId()));
	}

	@Test
	public void shouldNotExistsWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpost = journalpostRepository.save(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertTrue(journalpostRepository.existsById(journalpost.getId()));
		assertFalse(journalpostRepositorySkjermet.existsById(journalpost.getId()));
	}

	@Test
	public void shouldNotfindJournalpostIdByDokumentinfoIdWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpost = journalpostRepository.save(journalpost);
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(journalpostRepository.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), notNullValue());
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), nullValue());
	}

	@Test
	public void shouldNotfindJournalpostIdByTilleggsopplysningerNokkelAndVerdiWhenJournalpostIsSkjermet() {

		Journalpost journalpost = createJournalpost();

		journalpostRepository.save(journalpost);
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(journalpostRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), is(journalpost
				.getJournalpostId()));
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), nullValue());
	}

	@Test
	public void shouldNotfindJournalpostIdByKanalReferanseIdAndMottakskanalWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpost = journalpostRepository.save(journalpost);
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(journalpostRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(1));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO)
				.size(), is(0));

		assertThat(journalpostRepository.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO.name())
				.isPresent(), is(true));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MottaksKanalCode.NAV_NO
				.name()).isPresent(), is(false));
	}

	@Test
	public void shouldNotfindJournalpostByKanalReferanseIdWhenJournalpostIsSkjermet() {

		Journalpost journalpost = createJournalpost();

		journalpostRepository.save(journalpost);
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(journalpostRepository.findTopByKanalReferanseId(KANAL_REFERANSE_ID).isPresent(), is(true));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent(), is(false));
	}


	@Test
	public void shouldNotReturnSkjermetJournalpostIdForFindAllJournalpostIdsByDokumentInfoId() {
		Journalpost journalpost = createUniqueJournalpost();
		journalpostRepository.save(journalpost);

		Journalpost journalpostSkjermet = createUniqueJournalpost();

		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		//Legg til dokumentInfo som vedlegg til skjermet journalpost
		journalpostSkjermet.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.opprettetKildeNavn("test")
				.tilknyttetAvNavn("test")
				.dokumentInfo(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
				.build());

		journalpostSkjermet = journalpostRepository.save(journalpostSkjermet);
		skjermingService.setJournalpostSkjerming(journalpostSkjermet.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThat(journalpostRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId).size(), is(2));
		assertThat(journalpostRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId).size(), is(1));
		assertThat(journalpostRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId)
				.get(0), is(journalpost.getJournalpostId()));

	}

	static Journalpost createUniqueJournalpost() {
		return createJournalpost("123", DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.PEN)
				.kanalReferanseId(KANAL_REFERANSE_ID + UUID.randomUUID())
				.build();
	}
}