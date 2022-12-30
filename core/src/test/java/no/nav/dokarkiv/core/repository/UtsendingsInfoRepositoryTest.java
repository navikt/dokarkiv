package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;

import javax.transaction.Transactional;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDummyDokumentKassert;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, SkjermingServiceTest.class, JdbcAbacSecurityRepository.class})
@Transactional
@ActiveProfiles("itest")
public class UtsendingsInfoRepositoryTest {
	@Autowired
	UtsendingsInfoTestRepository utsendingsInfoTestRepository;
	@Autowired
	DokumentFilTestRepository dokumentFilTestRepository;
	@Autowired
	JoarkRepository joarkRepository;

	@BeforeEach
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@AfterEach
	public void cleanUp() {
		TestTransaction.end();
		dokumentFilTestRepository.deleteAll();
		utsendingsInfoTestRepository.deleteAll();
		joarkRepository.deleteAll();
	}

	@Test
	public void shouldSuccessfullySaveUtsendingsinfoForJournalpost() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilTestRepository.persist(arkivDokumentFil);
		dokumentFilTestRepository.persist(createDummyDokumentKassert());
		journalpost = joarkRepository.save(journalpost);

		var utsendingsInfoPart = new UtsendingsInfo.NavNoVarsling("navno-identifikator-for-mottaker",
				"Hei Bruker! Du har fått en ny melding på nav.no. Hilsen NAV");
		journalpost.setUtsendingskanal(UtsendingsKanalCode.NAV_NO);
		journalpost.setUtsendingsInfo(utsendingsInfoPart);
		journalpost = joarkRepository.save(journalpost);
		UtsendingsInfo utsendingsInfo = journalpost.getUtsendingsInfo();


		assertThat(utsendingsInfo.getId(), equalTo(journalpost.getId()));
		assertTrue(utsendingsInfoTestRepository.findById(journalpost.getJournalpostId()).isPresent(), "Det skal finnes en utsendingsinfo med journalpostId som id");
	}

	@Test
	public void shouldValidateUtsendingskanalUtsendingsinfoCombination() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilTestRepository.persist(arkivDokumentFil);
		dokumentFilTestRepository.persist(createDummyDokumentKassert());
		joarkRepository.save(journalpost);

		journalpost.setUtsendingskanal(UtsendingsKanalCode.NAV_NO);
		assertThrows(IllegalArgumentException.class, () ->
				journalpost.setUtsendingsInfo(new UtsendingsInfo.FysiskPostadresse("varslegate 1",
						null, null, "0101", "Oslo", "NO")));

		assertThrows(IllegalArgumentException.class, () ->
				journalpost.setUtsendingsInfo(new UtsendingsInfo.DigitalPostadresse("postmottaker#1323",
						"postkasseleverandør")));

		journalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		assertThrows(IllegalArgumentException.class, () ->
				journalpost.setUtsendingsInfo(new UtsendingsInfo.NavNoVarsling("navno-identifikator-for-mottaker",
						"Hei Bruker! Du har fått en ny melding på nav.no. Hilsen NAV")));
	}
}
