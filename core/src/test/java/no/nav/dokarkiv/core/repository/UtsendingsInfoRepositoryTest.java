package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDummyDokumentKassert;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, SkjermingServiceTest.class, JdbcAbacSecurityRepository.class})
@Transactional
@ActiveProfiles("itest")
public class UtsendingsInfoRepositoryTest {
	@Inject
	UtsendingsInfoRepository utsendingsInfoRepository;
	@Inject
	DokumentFilRepository dokumentFilRepository;
	@Inject
	JoarkRepository joarkRepository;

	@BeforeEach
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@AfterEach
	public void cleanUp() {
		TestTransaction.end();
		dokumentFilRepository.deleteAll();
		utsendingsInfoRepository.deleteAll();
		joarkRepository.deleteAll();
	}

	@Test
	public void shouldSuccessfullySaveUtsendingsinfoForJournalPost() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(createDummyDokumentKassert());
		journalpost = joarkRepository.save(journalpost);

		UtsendingsInfo utsendingsInfo = new UtsendingsInfo(journalpost, new UtsendingsInfo.DigitalPostadresse("post.mottaker#1241", "959844519"));
		utsendingsInfo = utsendingsInfoRepository.save(utsendingsInfo);


		assertThat(utsendingsInfo.getId(), equalTo(journalpost.getId()));
		assertTrue(utsendingsInfoRepository.findById(journalpost.getJournalpostId()).isPresent(), "Det skal finnes en utsendingsinfo med journalpostId som id");
	}
}
