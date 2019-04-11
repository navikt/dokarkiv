package no.nav.dokarkiv.core.akjsonslogg;

import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_ARKIVELEMENT;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_BRUKER;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_FRA_VERDI;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_TIL_VERDI;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_UTFOERT_AV;
import static no.nav.dokarkiv.core.util.TestDataUtils.APPLICATION;
import static no.nav.dokarkiv.core.util.TestDataUtils.USER_ID;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggTO;
import static no.nav.dokarkiv.core.util.TestDataUtils.createArkivElementEndringToList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggServiceImpl;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.core.util.TestDataUtils;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, AksjonsLoggServiceImpl.class, SkjermingService.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@EnableConfigurationProperties
@Transactional
@ActiveProfiles("itest")
public class AksjonsLoggIT {

	@Inject
	private AksjonsLoggService aksjonsLoggService;

	@Inject
	private AksjonsLoggRepository aksjonsLoggRepository;

	@Inject
	private JoarkRepository joarkRepository;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private long journalpostId;

	@Before
	public void setUp() {
		RequestContextUtil.createAndSetUsername(USER_ID, APPLICATION);
		aksjonsLoggRepository.deleteAll();
		Journalpost journalpost = joarkRepository.save(TestDataGenerator.createJournalpostWithHoveddokument());
		this.journalpostId = journalpost.getJournalpostId();
	}

	@Test
	public void shouldSaveAksjonsLogg() throws UgyldigAksjonsLoggException {

		aksjonsLoggService.validateAndSaveAksjonsLogg(createAksjonsLoggTO(journalpostId, 1L), createArkivElementEndringToList());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);

		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.ARKIVERING));
		assertThat(aksjonsLogg.getBruker(), is(AKSJON_BRUKER));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(1L));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpostId));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getArkivsaksnummer(), is(TestDataGenerator.SAK_ID));
		assertThat(aksjonsLogg.getArkivsaksystem(), is(FagsystemCode.AO01));
		assertThat(Duration.between(aksjonsLogg.getTidspunkt(), LocalDateTime.now()).getSeconds(), lessThan(10L));

		assertThat(aksjonsLogg.getUtfoertAv(), is(AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getApplikasjon(), is(APPLICATION));

		ArkivElementEndring arkivElementEndring = aksjonsLogg.getArkivElementEndringer().iterator().next();
		assertThat(arkivElementEndring.getAksjonsLogg(), is(aksjonsLogg));
		assertThat(arkivElementEndring.getArkivElement(), is(AKSJON_ARKIVELEMENT));
		assertThat(arkivElementEndring.getFraVerdi(), is(AKSJON_FRA_VERDI));
		assertThat(arkivElementEndring.getTilVerdi(), is(AKSJON_TIL_VERDI));
		assertThat(Duration.between(arkivElementEndring.getTidspunkt(), LocalDateTime.now()).getSeconds(), lessThan(10L));
	}


	@Test
	public void shouldMapUtfoertAvFromRequestContextIfUtfoertAvIsNull() throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTO = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTO.setUtfoertAv(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, createArkivElementEndringToList());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);

		assertThat(aksjonsLogg.getUtfoertAv(), is(USER_ID));
		assertThat(aksjonsLogg.getApplikasjon(), is(APPLICATION));
	}

	@Test
	public void shouldThrowWhenAksjonIsNull() throws UgyldigAksjonsLoggException {
		expectedException.expect(UgyldigAksjonsLoggException.class);
		expectedException.expectMessage("aksjon");

		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTOList.setAksjon(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, createArkivElementEndringToList());
	}

	@Test
	public void shouldThrowWhenUtfoertAvIsNull() throws UgyldigAksjonsLoggException {
		expectedException.expect(UgyldigAksjonsLoggException.class);
		expectedException.expectMessage("AksjonsLogg mangler påkrevd parameter: utfoertAv. AksjonsLogg input må inneholde parameteren \"utfoertAv\" hvis kallet ikke inneholder sikkerhetstoken for saksbehandleren");
		RequestContextUtil.createAndSetUsername(APPLICATION, APPLICATION);

		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTOList.setUtfoertAv(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, createArkivElementEndringToList());
	}

	@Test
	public void shouldThrowWhenBrukerIsNull() throws UgyldigAksjonsLoggException {
		expectedException.expect(UgyldigAksjonsLoggException.class);
		expectedException.expectMessage("bruker");

		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTOList.setBruker(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, createArkivElementEndringToList());
	}

	@Test
	public void shouldThrowWhenJournalpostIdAndDokumentInfoIdIsNull() throws UgyldigAksjonsLoggException {
		expectedException.expect(UgyldigAksjonsLoggException.class);
		expectedException.expectMessage("AksjonsLogg mangler påkrevd parameter: enten journalpostId eller dokumentInfoId må bli satt.");

		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTOList.setDokumentInfoId(null);
		aksjonsLoggTOList.setJournalpostId(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, createArkivElementEndringToList());
	}

	@Test
	public void shouldThrowWhenArkivElementEndringArkivElementIsNull() throws UgyldigAksjonsLoggException {
		expectedException.expect(UgyldigAksjonsLoggException.class);
		expectedException.expectMessage("AksjonsLogg.ArkivElementEndring mangler påkrevd parameter: arkivElement");

		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		List<ArkivElementEndringTO> arkivElementEndringTO =  createArkivElementEndringToList();
		arkivElementEndringTO.get(0).setArkivElement(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList,arkivElementEndringTO);
	}

	@Test
	public void shouldThrowWhenArkivElementEndringArkivElementFraVerdiAndTilVerdiIsNull() throws UgyldigAksjonsLoggException {
		expectedException.expect(UgyldigAksjonsLoggException.class);
		expectedException.expectMessage("Ugyldig AksjonsLogg.ArkivElementEndring: enten fraVerdi eller tilVerdi må bli satt");

		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		List<ArkivElementEndringTO> arkivElementEndringTO =  createArkivElementEndringToList();
		arkivElementEndringTO.get(0).setFraVerdi(null);
		arkivElementEndringTO.get(0).setTilVerdi(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, arkivElementEndringTO);
	}

}
