package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
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
	private JournalpostTestRepository journalpostTestRepository;

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
		journalpostTestRepository.deleteAll();
		entityManager.createNativeQuery("Delete from t_jp_tillegg").getFirstResult();
	}

	@Test
	public void shouldReturnNullOrFalseWhenNotFound() {
		assertThat(journalpostRepositorySkjermet.findById(123L).isPresent(), is(false));
		assertThat(journalpostRepositorySkjermet.existsById(123L), is(false));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseId("test").isPresent(), is(false));
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByDokumentinfoId("213"), nullValue());
		assertThat(journalpostRepositorySkjermet.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi("213", "313"), nullValue());
	}

	@Test
	public void shouldReturnSameResultAsRepositoryWhenNotSkjermet() {

		Journalpost journalpost = createJournalpost();
		journalpost = journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertTrue(journalpostTestRepository.existsById(journalpost.getId()));
		assertTrue(journalpostRepositorySkjermet.existsById(journalpost.getId()));

		assertThat(journalpostTestRepository.findById(journalpost.getId()).isPresent(), is(true));
		assertThat(journalpostRepositorySkjermet.findById(journalpost.getId()).isPresent(), is(true));

		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllJournalpostIdsByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));
		assertThat(journalpostRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		assertThat(dokumentInfoTestRepository.findDokumentInfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());
		assertThat(journalpostRepositorySkjermet.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE), notNullValue());

		assertThat(dokumentInfoTestRepository.findOriginalJournalpostIdByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()), notNullValue());
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), notNullValue());

		assertTrue(journalpostTestRepository.findByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
		assertTrue(journalpostRepositorySkjermet.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent());
	}

	@Test
	public void shouldNotReturnSkjermetJournalpostForFindById() {
		Journalpost journalpost = createJournalpost();


		journalpost = journalpostTestRepository.persist(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThat(journalpostTestRepository.findById(journalpost.getId()).isPresent(), is(true));
		assertThat(journalpostRepositorySkjermet.findById(journalpost.getId()).isPresent(), is(false));

	}

	@Test
	public void shouldNotExistsWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpost = journalpostTestRepository.persist(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertTrue(journalpostTestRepository.existsById(journalpost.getId()));
		assertFalse(journalpostRepositorySkjermet.existsById(journalpost.getId()));
	}

	@Test
	public void shouldNotfindJournalpostIdByDokumentinfoIdWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpost = journalpostTestRepository.persist(journalpost);
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(dokumentInfoTestRepository.findOriginalJournalpostIdByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()), notNullValue());
		assertThat(journalpostRepositorySkjermet.findJournalpostIdByDokumentinfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getId()
				.toString()), nullValue());
	}

	@Test
	public void shouldNotfindJournalpostByKanalReferanseIdWhenJournalpostIsSkjermet() {
		Journalpost journalpost = createJournalpost();

		journalpostTestRepository.persist(journalpost);
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(journalpostTestRepository.findByKanalReferanseId(KANAL_REFERANSE_ID).isPresent(), is(true));
		assertThat(journalpostRepositorySkjermet.findJournalpostByKanalReferanseId(KANAL_REFERANSE_ID).isPresent(), is(false));
	}

	@Test
	public void shouldNotReturnSkjermetJournalpostIdForFindAllJournalpostIdsByDokumentInfoId() {
		Journalpost journalpost = createUniqueJournalpost();
		journalpostTestRepository.persist(journalpost);

		Journalpost journalpostSkjermet = createUniqueJournalpost();

		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		//Legg til dokumentInfo som vedlegg til skjermet journalpost
		journalpostSkjermet.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.opprettetKildeNavn("test")
				.tilknyttetAvNavn("test")
				.dokumentInfo(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
				.build());

		journalpostSkjermet = journalpostTestRepository.persist(journalpostSkjermet);
		skjermingService.setJournalpostSkjerming(journalpostSkjermet.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId).size(), is(2));
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