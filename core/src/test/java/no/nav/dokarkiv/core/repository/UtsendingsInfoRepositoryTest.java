package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
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

import javax.transaction.Transactional;
import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDummyDokumentKassert;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, SkjermingServiceTest.class})
@Transactional
@ActiveProfiles("itest")
public class UtsendingsInfoRepositoryTest {
	@Autowired
	UtsendingsInfoTestRepository utsendingsInfoTestRepository;
	@Autowired
	DokumentFilTestRepository dokumentFilTestRepository;
	@Autowired
	JournalpostTestRepository journalpostTestRepository;

	@BeforeEach
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@AfterEach
	public void cleanUp() {
		TestTransaction.end();
		dokumentFilTestRepository.deleteAll();
		utsendingsInfoTestRepository.deleteAll();
		journalpostTestRepository.deleteAll();
	}

	@Test
	public void shouldSuccessfullySaveUtsendingsinfoForJournalpost() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilTestRepository.persist(arkivDokumentFil);
		dokumentFilTestRepository.persist(createDummyDokumentKassert());
		journalpost = journalpostTestRepository.persist(journalpost);

		var utsendingsInfoPart = new UtsendingsInfo.NavNoVarsling("navno-identifikator-for-mottaker", null);
		var epostvarsel = new UtsendingsInfo.EpostVarsler(List.of(new UtsendingsInfo.EpostVarsel("tittel", "tekst", "homer@epos.gr", "2023-02-27T12:30:00.000")));
		var smsvarsel = new UtsendingsInfo.SmsVarsler(List.of(new UtsendingsInfo.SmsVarsel("tekst", "+4700000000", "2023-02-27T12:30:00.000")));
		UtsendingsInfo utsendingsInfo = utsendingsInfoTestRepository.persistAndFlush(new UtsendingsInfo(journalpost, utsendingsInfoPart, epostvarsel, smsvarsel));

		assertThat(utsendingsInfo.getJournalpostId(), equalTo(journalpost.getId()));
		assertTrue(utsendingsInfoTestRepository.findById(journalpost.getJournalpostId()).isPresent(), "Det skal finnes en Utsendingsinfo med journalpostId som id");
	}
}
