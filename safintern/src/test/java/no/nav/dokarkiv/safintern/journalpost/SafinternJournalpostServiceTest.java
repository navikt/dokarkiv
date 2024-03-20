package no.nav.dokarkiv.safintern.journalpost;

import no.nav.dokarkiv.core.domain.codes.Fagomrade;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.FagomradeTestRepository;
import no.nav.dokarkiv.core.repository.JournalpostTestRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.repository.SakTestRepository;
import no.nav.dokarkiv.core.repository.UtsendingsInfoTestRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.safintern.SafinternConfig;
import no.nav.dokarkiv.safintern.views.DokumentinfoView;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Set;

import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.RPO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.S;
import static no.nav.dokarkiv.safintern.journalpost.TestdataAsserter.assertBruker;
import static no.nav.dokarkiv.safintern.journalpost.TestdataAsserter.assertSak;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.SKJERMING_TYPE_CODE;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFysiskpostUtsendingsInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createGsak;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ContextConfiguration(classes = {
		RepositoryConfig.class,
		SkjermingService.class,
		SkjermingServiceTest.class,
		SafinternConfig.class}
)
@ActiveProfiles("itest")
public class SafinternJournalpostServiceTest {

	@Autowired
	private EntityManager entityManager;
	@Autowired
	private JournalpostTestRepository journalpostTestRepository;
	@Autowired
	private SakTestRepository sakTestRepository;
	@Autowired
	private UtsendingsInfoTestRepository utsendingsInfoTestRepository;
	@Autowired
	private FagomradeTestRepository fagomradeTestRepository;
	@Autowired
	private SafinternJournalpostService safinternJournalpostService;

	@BeforeEach
	void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
		entityManager.createNativeQuery("""
				INSERT INTO T_K_BEHANDLINGSTEMA (k_behandlingstema,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av) VALUES ('ab0438','Lønnskompensasjon',date '1900-01-01',NULL,'1',timestamp '2018-10-05 13:00:00','itest',timestamp '2018-10-05 13:00:00','itest');
				""").executeUpdate();
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("RPO")
						.dekode("Retting av personopplysninger")
						.erGyldig(false)
						.datoTilOgMed(LocalDate.of(2023, 5, 1))
						.build());
	}

	@Test
	void shouldFetch() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(S);
		journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);

		JournalpostView journalpostView = safinternJournalpostService.hentJournalpostById(actualJournalpost.getJournalpostId(), Set.of("status", "mottakskanal"));

		assertThat(journalpostView.getMottakskanal()).isEqualTo(NAV_NO);
		assertThat(journalpostView.getStatus()).isEqualTo(FS);
		assertThat(journalpostView.getSaksrelasjon()).isNull();
		assertThat(journalpostView.getUtsendingskanal()).isNull();
	}

	@Test
	void shouldFetchDokumenterPaths() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(S);
		journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);

		JournalpostView journalpostView = safinternJournalpostService.hentJournalpostById(actualJournalpost.getJournalpostId(), Set.of("bruker", "saksrelasjon", "fagomraade", "status", "skjerming", "dokumenter.skjerming"));

		assertThat(journalpostView.getFagomraade()).isEqualTo(RPO);
		assertThat(journalpostView.getStatus()).isEqualTo(FS);
		assertThat(journalpostView.getSkjerming()).isEqualTo(SKJERMING_TYPE_CODE);
		assertSak(sakId, journalpostView.getSaksrelasjon());
		assertBruker(journalpostView.getBruker());
		assertThat(journalpostView.getDokumenter()).hasSize(2);
		assertThat(journalpostView.getDokumenter()).extracting(DokumentinfoView::getSkjerming).containsExactly(null, POL);
	}

	@Test
	void shouldThrowExceptionWhenFetchValueNotFound() {
		assertThatThrownBy(() ->
						safinternJournalpostService.hentJournalpostById(123L, Set.of("utsendingsinfo")),
				"fetch verdier må være godkjent av FetchPaths")
				.isInstanceOf(IllegalArgumentException.class);
	}
}
