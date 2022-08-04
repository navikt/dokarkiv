package no.nav.dokarkiv.core.repository.journalpostliste;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, JournalpostListeRepository.class})
@Transactional
@ActiveProfiles("itest")
public class JournalpostListeRepositoryIT {

	private DateTime earliestInnsynDato = new DateTime(2015, 5, 1, 0, 0);
	private JournalStatusCode[] journalStatusCodesAllowed =
			new JournalStatusCode[]{JournalStatusCode.J, JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E};

	private final DateTime journalDato = new DateTime(2016, 5, 1, 0, 0);
	private final FagsystemCode fagsystem = FagsystemCode.PEN;
	private final String saksnr = "123";

	private HentMinJPListeParameters hentMinJPListeParameters;

	@Inject
	private JournalpostListeRepository journalpostListeRepository;
	@Inject
	private JoarkRepositorySkjermet joarkRepository;

	@BeforeEach
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
		hentMinJPListeParameters = createHentMinJPListeParameters();
	}

	@Test
	public void shouldRestrictToJournalAfterDato() {
		// CreatedDate is automatically populated, so we have to set tidligstInnsynDato to future date
		createJournalpost(DateTime.now().minusDays(2).toDate(), JournalStatusCode.J, FagomradeCode.PEN);
		createJournalpost(DateTime.now().plusDays(2).toDate(), JournalStatusCode.J, FagomradeCode.PEN);

		hentMinJPListeParameters.setTidligstInnsynDato(DateTime.now().plusDays(1).toDate());
		hentMinJPListeParameters.addFagsystemSak(saksnr, fagsystem);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(1));
	}

	@Test
	public void shouldRestrictOnJournalStatusCode() {
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.PEN);
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.FS, FagomradeCode.PEN);
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.FL, FagomradeCode.AAR);
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.E, FagomradeCode.AAP);

		createJournalpost(DateTime.now().toDate(), JournalStatusCode.A, FagomradeCode.ENF);
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.R, FagomradeCode.FEI);

		hentMinJPListeParameters.addFagsystemSak(saksnr, fagsystem);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(4));
	}

	@Test
	public void shouldRestrictToNotFeilregistrert() {
		createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22);
		createJournalpostWithSaksrelasjon("1338", false, null, FagsystemCode.FS22);
		createJournalpostWithSaksrelasjon("1339", true, null, FagsystemCode.FS22);

		hentMinJPListeParameters.addFagsystemSak("1337", FagsystemCode.FS22);
		hentMinJPListeParameters.addFagsystemSak("1338", FagsystemCode.FS22);
		hentMinJPListeParameters.addFagsystemSak("1339", FagsystemCode.FS22);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(2));
	}

	@Test
	public void shouldReturnEmptyWhenNotFeilRegistrertAndCreatedDatoBefore() {
		createJournalpostWithSaksrelasjon("1339", true, null, FagsystemCode.FS22);
		hentMinJPListeParameters.addFagsystemSak("1339", FagsystemCode.FS22);
		hentMinJPListeParameters.setTidligstInnsynDato(journalDato.minusDays(1).toDate());

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);

		assertThat(journalpostListe, Matchers.hasSize(0));
	}

	@Test
	public void shouldRestrictToNotFagomradeKontroll() {
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.KTR);
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.AAR);
		hentMinJPListeParameters.addFagsystemSak(saksnr, fagsystem);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(1));
	}

	@Test
	public void shouldRestrictToDefinedFagomrade() {
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.KTR);
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.AAR);
		Journalpost penJournalpost = createJournalpost(DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.PEN);
		Journalpost forJournalpost = createJournalpost(DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.FOR);
		hentMinJPListeParameters.addFagsystemSak(saksnr, fagsystem);
		hentMinJPListeParameters.getFagomraade().addAll(Arrays.asList(FagomradeCode.PEN, FagomradeCode.FOR));

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(2));
		assertThat(journalpostListe, CoreMatchers.hasItem(penJournalpost));
		assertThat(journalpostListe, CoreMatchers.hasItem(forJournalpost));
	}

	@Test
	public void shouldSearchWhenPagingParamsIsMissingl() {
		int i = 0;
		while (5 > i++) {
			createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22);

		}
		journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
	}

	@Test
	public void shouldReturnMaxResult() {
		int i = 0;
		while (5 > i++) {
			createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22);

		}

		hentMinJPListeParameters.addFagsystemSak("1337", FagsystemCode.FS22);
		hentMinJPListeParameters.setMaxResults(3);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(3));
	}


	@Test
	public void shouldReturnFirstPage() {
		List<Journalpost> expectedJournalposter = new LinkedList<Journalpost>();
		int i = 0;
		while (5 > i++) {
			expectedJournalposter.add(createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22));
		}

		hentMinJPListeParameters.addFagsystemSak("1337", FagsystemCode.FS22);
		hentMinJPListeParameters.setMaxResults(3);
		hentMinJPListeParameters.setPageNr(0);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(3));
		assertThat(journalpostListe, CoreMatchers.hasItem(expectedJournalposter.get(0)));
		assertThat(journalpostListe, CoreMatchers.hasItem(expectedJournalposter.get(1)));
		assertThat(journalpostListe, CoreMatchers.hasItem(expectedJournalposter.get(2)));
	}

	@Test
	public void shouldReturn2ndPage() {
		List<Journalpost> expectedJournalposter = new LinkedList<Journalpost>();
		int i = 0;
		while (5 > i++) {
			expectedJournalposter.add(createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22));
		}
		hentMinJPListeParameters.addFagsystemSak("1337", FagsystemCode.FS22);
		hentMinJPListeParameters.setMaxResults(3);
		hentMinJPListeParameters.setPageNr(1);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(2));
		assertThat(journalpostListe, CoreMatchers.hasItem(expectedJournalposter.get(3)));
		assertThat(journalpostListe, CoreMatchers.hasItem(expectedJournalposter.get(4)));

	}

	@Test
	public void shouldReturnEmptyListPageNrOutOfResultSetSize() {
		int i = 0;
		while (5 > i++) {
			createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22);
			createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.PEN);
		}

		hentMinJPListeParameters.addFagsystemSak("1337", FagsystemCode.FS22);
		hentMinJPListeParameters.setMaxResults(3);
		hentMinJPListeParameters.setPageNr(2);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(0));


	}

	@Test
	public void shouldFindTotalNumberOfJournalposts() {
		int i = 0;
		while (5 > i++) {
			createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22);
		}

		hentMinJPListeParameters.addFagsystemSak("1337", FagsystemCode.FS22);

//		assertThat(joarkRepository.findTotalNumberOfJournalposts(hentMinJPListeParameters), Matchers.is(5L));
	}

	@Test
	public void shouldRestrictToSaksIdAndFagSystem() {
		createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22);
		createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22);
		createJournalpostWithSaksrelasjon("1338", false, null, FagsystemCode.FS22);

		hentMinJPListeParameters.addFagsystemSak("1337", FagsystemCode.FS22);

		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		assertThat(journalpostListe, Matchers.hasSize(2));
	}

	@Test
	public void shouldRestrictToJournalpostType() {
		//GIVEN
		createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22, JournalpostTypeCode.I);
		createJournalpostWithSaksrelasjon("1337", false, null, FagsystemCode.FS22);
		createJournalpostWithSaksrelasjon("1338", false, null, FagsystemCode.FS22);

		hentMinJPListeParameters.addFagsystemSak("1337", FagsystemCode.FS22);
		hentMinJPListeParameters.setJournalpostTypeCode(JournalpostTypeCode.I);

		//WHEN
		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);

		//THEN
		assertThat(journalpostListe, Matchers.hasSize(1));
	}


	@Test
	public void shouldRestrictToFomToTomJournalDate() {
		//GIVEN
		Date shouldBeFoundDate = DateTime.now().plusDays(2).toDate();
		createJournalpost(DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.PEN); //journalFom should ignore this
		createJournalpost(shouldBeFoundDate, JournalStatusCode.J, FagomradeCode.AAP);
		createJournalpost(DateTime.now().plusDays(10).toDate(), JournalStatusCode.J, FagomradeCode.AAP);//journalTom should ignore this
		hentMinJPListeParameters.addFagsystemSak(saksnr, fagsystem);
		hentMinJPListeParameters.setJournalFom(DateTime.now().plusDays(1).toDate());
		hentMinJPListeParameters.setJournalTom(DateTime.now().plusDays(4).toDate());

		//WHEN
		List<Journalpost> journalpostListe = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);

		//THEN
		assertThat(journalpostListe, Matchers.hasSize(1));
		assertThat(journalpostListe.get(0).getChangeStamp().getCreatedDate(), Matchers.is(shouldBeFoundDate));

	}

	private Journalpost createJournalpostWithSaksrelasjon(String saksnr, boolean isFeilregistrert, FagomradeCode fagomrade,
														  FagsystemCode fagsystem) {
		return joarkRepository.save(TestDataUtils.createJournalpostWithSaksrelasjon(saksnr, isFeilregistrert, fagomrade, fagsystem, JournalpostTypeCode.U).build());
	}

	private Journalpost createJournalpostWithSaksrelasjon(String saksnr, boolean isFeilregistrert, FagomradeCode fagomrade,
														  FagsystemCode fagsystem, JournalpostTypeCode journalpostType) {
		return joarkRepository.save(TestDataUtils.createJournalpostWithSaksrelasjon(saksnr, isFeilregistrert, fagomrade, fagsystem, journalpostType).build());
	}

	private Journalpost createJournalpost(Date date, JournalStatusCode journalStatus, FagomradeCode fagomradeCode) {
		return joarkRepository.save(TestDataUtils.createJournalpost(saksnr, date, journalStatus, fagomradeCode).build());
	}

	private HentMinJPListeParameters createHentMinJPListeParameters() {
		HentMinJPListeParameters parameters = new HentMinJPListeParameters();
		parameters.setTidligstInnsynDato(earliestInnsynDato.toDate());
		parameters.setTillattInnsynStatus(Lists.newArrayList(journalStatusCodesAllowed));
		parameters.setVisFeilRegistrert(false);
		List<FagomradeCode> skjulFagomraade = Lists.newArrayList();
		skjulFagomraade.add(FagomradeCode.KTR);
		parameters.setSkjulFagomraade(skjulFagomraade);
		return parameters;
	}

}
