package no.nav.dokarkiv.core.repository.journalpostliste;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataUtils;
import org.joda.time.DateTime;
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
import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, SkjermingService.class, JournalpostListeRepository.class})
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
	private JournalpostListeRepository journalpostListeRepository;

	@Inject
	private SkjermingService skjermingService;

	@Before
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	private final String SAKID = "123";
	private final FagomradeCode FAGOMRADE = FagomradeCode.PEN;


	@After
	public void cleanUp() {
		TestTransaction.end();
		journalpostDokumentInfoRelasjonRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		joarkRepository.deleteAll();
	}

	@Test
	public void shouldNotCountJournalpostWhenBegrenset() {
		Journalpost journalpostBegrenset = TestDataUtils.createJournalpost(SAKID, DateTime.now()
				.toDate(), JournalStatusCode.J, FAGOMRADE).build();
		Journalpost journalpost = TestDataUtils.createJournalpost(SAKID, DateTime.now()
				.toDate(), JournalStatusCode.J, FAGOMRADE).build();
		joarkRepository.save(journalpost);
		joarkRepository.save(journalpostBegrenset);
		skjermingService.setJournalpostSkjermet(journalpostBegrenset, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		HentMinJPListeParameters hentMinJPListeParameters = new HentMinJPListeParameters();
		hentMinJPListeParameters.setSaksListe(Arrays.asList(new SakFagsystem(TestDataUtils.fagsystem, SAKID)));

		Long totalNumberOfJournalpostsBegrenset = journalpostListeRepository.findTotalNumberOfJournalposts(hentMinJPListeParameters);


		hentMinJPListeParameters.setIncludeBegrensetJournalpost(true);
		Long totalNumberOfJournalpostsAll = journalpostListeRepository.findTotalNumberOfJournalposts(hentMinJPListeParameters);

		assertThat(totalNumberOfJournalpostsBegrenset, is(1L));
		assertThat(totalNumberOfJournalpostsAll, is(2L));
	}

	@Test
	public void shouldNotGetJournalpostWhenBegrenset() {
		Journalpost journalpostBegrenset = TestDataUtils.createJournalpost(SAKID, DateTime.now()
				.toDate(), JournalStatusCode.J, FAGOMRADE).build();
		Journalpost journalpost = TestDataUtils.createJournalpost(SAKID, DateTime.now()
				.toDate(), JournalStatusCode.J, FAGOMRADE).build();
		joarkRepository.save(journalpost);
		joarkRepository.save(journalpostBegrenset);
		skjermingService.setJournalpostSkjermet(journalpostBegrenset, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		HentMinJPListeParameters hentMinJPListeParameters = new HentMinJPListeParameters();
		hentMinJPListeParameters.setSaksListe(Arrays.asList(new SakFagsystem(TestDataUtils.fagsystem, SAKID)));

		List<Journalpost> journalpostListBegrenset = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);


		hentMinJPListeParameters.setIncludeBegrensetJournalpost(true);
		List<Journalpost> journalpostListBegrensetAll = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);

		assertThat(journalpostListBegrenset.size(), is(1));
		assertThat(journalpostListBegrenset.get(0).getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(journalpostListBegrensetAll.size(), is(2));
	}


}