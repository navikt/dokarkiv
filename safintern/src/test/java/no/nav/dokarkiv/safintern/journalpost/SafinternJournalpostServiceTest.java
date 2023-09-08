package no.nav.dokarkiv.safintern.journalpost;

import no.nav.dokarkiv.core.domain.codes.Fagomrade;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
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
import java.util.List;

import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFysiskpostUtsendingsInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createGsak;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, SkjermingServiceTest.class, SafinternConfig.class})
@Transactional
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
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);

		JournalpostView journalpostView = safinternJournalpostService.hentJournalpostById(actualJournalpost.getJournalpostId(), List.of("status", "mottakskanal"));

		assertThat(journalpostView.getMottakskanal()).isEqualTo(MottaksKanalCode.NAV_NO);
		assertThat(journalpostView.getStatus()).isEqualTo(JournalStatusCode.FS);
		assertThat(journalpostView.getSaksrelasjon()).isNull();
		assertThat(journalpostView.getUtsendingskanal()).isNull();
	}
}
