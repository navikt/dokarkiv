package no.nav.dokarkiv.core.aksjonslogg;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.AksjonsLoggTestRepository;
import no.nav.dokarkiv.core.repository.JournalpostTestRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.core.util.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.OPPRETTET_KILDE_NAVN;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, AksjonsLoggServiceImpl.class, SkjermingService.class})
@EnableConfigurationProperties
@Transactional
@ActiveProfiles("itest")
public class AksjonsLoggIT {

	@Autowired
	private AksjonsLoggService aksjonsLoggService;

	@Autowired
	private AksjonsLoggTestRepository aksjonsLoggTestRepository;

	@Autowired
	private JournalpostTestRepository journalpostTestRepository;

	private long journalpostId;

	@BeforeEach
	public void setUp() {
		RequestContextUtil.createAndSetUsername(USER_ID, APPLICATION);
		aksjonsLoggTestRepository.deleteAll();
		Journalpost journalpost = journalpostTestRepository.persist(TestDataGenerator.createJournalpostWithHoveddokument());
		this.journalpostId = journalpost.getJournalpostId();
	}

	@Test
	public void shouldSaveAksjonsLogg() throws UgyldigAksjonsLoggException {
		aksjonsLoggService.validateAndSaveAksjonsLogg(createAksjonsLoggTO(journalpostId, 1L), createArkivElementEndringToList());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size(), is(1));
		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);

		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.ARKIVERING));
		assertThat(aksjonsLogg.getBruker(), is(AKSJON_BRUKER));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(1L));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpostId));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getArkivsaksnummer(), is(TestDataGenerator.API_GSAK_ID.toString()));
		assertThat(aksjonsLogg.getArkivsaksystem(), is(FagsystemCode.FS22));
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
	public void shouldGetBrukerFromJoark() throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTO = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTO.setBruker(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, createArkivElementEndringToList());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size(), is(1));
		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);

		assertThat(aksjonsLogg.getBruker(), is(BRUKER_ID));
	}

	@Test
	public void shouldGetMostRecentBruker() throws UgyldigAksjonsLoggException {
		Journalpost journalpost = journalpostTestRepository.findById(journalpostId)
				.orElseThrow(JournalpostIkkeFunnetException::new);
		String nyBrukerId = "12345678910";
		Bruker nyBruker = Bruker.builder()
				.brukerId(nyBrukerId)
				.brukerType(BrukerTypeCode.PERSON)
				.build();
		nyBruker.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpost.addBruker(nyBruker);
		journalpostTestRepository.persist(journalpost);

		AksjonsLoggTO aksjonsLoggTO = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTO.setBruker(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, createArkivElementEndringToList());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size(), is(1));
		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);

		assertThat(aksjonsLogg.getBruker(), is(nyBrukerId));
	}

	@Test
	public void shouldMapUtfoertAvFromRequestContextIfUtfoertAvIsNull() throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTO = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTO.setUtfoertAv(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, createArkivElementEndringToList());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size(), is(1));
		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);

		assertThat(aksjonsLogg.getUtfoertAv(), is(USER_ID));
		assertThat(aksjonsLogg.getApplikasjon(), is(APPLICATION));
	}

	@Test
	public void shouldThrowWhenAksjonIsNull() throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTOList.setAksjon(null);
		assertThrows(UgyldigAksjonsLoggException.class, () ->
				aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, createArkivElementEndringToList()), "aksjon");
	}

	@Test
	public void shouldGetUtfoertAvFromContextWhenNull() throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTOList.setUtfoertAv(null);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, createArkivElementEndringToList());
		String faktiskUtfoertAv = aksjonsLoggTestRepository.findAll().iterator().next().getUtfoertAv();
		assertEquals(USER_ID, faktiskUtfoertAv);
	}

	@Test
	public void shouldThrowWhenJournalpostIdAndDokumentInfoIdIsNull() throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		aksjonsLoggTOList.setDokumentInfoId(null);
		aksjonsLoggTOList.setJournalpostId(null);
		assertThrows(UgyldigAksjonsLoggException.class, () ->
						aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, createArkivElementEndringToList()),
				"AksjonsLogg mangler påkrevd parameter: enten journalpostId eller dokumentInfoId må bli satt.");
	}

	@Test
	public void shouldThrowWhenArkivElementEndringArkivElementIsNull() throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		List<ArkivElementEndringTO> arkivElementEndringTO = createArkivElementEndringToList();
		arkivElementEndringTO.get(0).setArkivElement(null);
		assertThrows(UgyldigAksjonsLoggException.class, () ->
						aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, arkivElementEndringTO),
				"AksjonsLogg.ArkivElementEndring mangler påkrevd parameter: arkivElement");
	}

	@Test
	public void shouldThrowWhenArkivElementEndringArkivElementFraVerdiAndTilVerdiIsNull() throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTOList = createAksjonsLoggTO(journalpostId, 1L);
		List<ArkivElementEndringTO> arkivElementEndringTO = createArkivElementEndringToList();
		arkivElementEndringTO.get(0).setFraVerdi(null);
		arkivElementEndringTO.get(0).setTilVerdi(null);
		assertThrows(UgyldigAksjonsLoggException.class, () ->
						aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTOList, arkivElementEndringTO),
				"Ugyldig AksjonsLogg.ArkivElementEndring: enten fraVerdi eller tilVerdi må bli satt");
	}

}
