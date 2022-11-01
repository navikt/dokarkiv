package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataGenerator;
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

import java.time.LocalDateTime;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.repository.DokumentFilSkjermetRepository.FIL_UUID_DUMMY_DOKUMENT_SKJERMET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.FIL_SLADDET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDummyDokumentKassert;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDummyDokumentSkjermet;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, SkjermingServiceTest.class, JdbcAbacSecurityRepository.class})
@Transactional
@ActiveProfiles("itest")
public class DokumentFilSkjermetRepositoryTest {
	@Autowired
	private JoarkRepository joarkRepository;

	@Autowired
	private DokumentinfoRepository dokumentinfoRepository;

	@Autowired
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Autowired
	private DokumentFilRepository dokumentFilRepository;

	@Autowired
	private DokumentFilSkjermetRepository dokumentFilSkjermetRepository;

	@Autowired
	private SkjermingServiceTest skjermingService;

	@BeforeEach
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@AfterEach
	public void cleanUp() {
		TestTransaction.end();
		dokumentFilRepository.deleteAll();
		journalpostDokumentInfoRelasjonRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		joarkRepository.deleteAll();
	}

	@Test
	public void shouldReturnDummyDokumentWhenKassertAndArkivVariantIsSkjermet() {

		Journalpost journalpost = createJournalpostWithHoveddokument();

		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(createDummyDokumentKassert());
		dokumentFilRepository.save(createDummyDokumentSkjermet());
		journalpost = joarkRepository.save(journalpost);

		DokumentFil dokumentFilBefore = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilBefore.getFil(), is(arkivDokumentFil.getFil()));

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.setKassert(true);
		dokumentinfoRepository.save(dokumentInfo);
		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), ARKIV, SkjermingTypeCode.POL);

		DokumentFil dokumentFilAfter = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilAfter.getFil(), is(TestDataGenerator.FIL_DUMMY_KASSERT));
	}

	@Test
	public void shouldReturnDummyDokumentSkjermetWhenArkivVariantIsSkjermet() {

		Journalpost journalpost = createJournalpostWithHoveddokument();

		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(createDummyDokumentKassert());
		dokumentFilRepository.save(createDummyDokumentSkjermet());
		journalpost = joarkRepository.save(journalpost);

		DokumentFil dokumentFilBefore = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilBefore.getFil(), is(arkivDokumentFil.getFil()));

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentinfoRepository.save(dokumentInfo);
		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), ARKIV, SkjermingTypeCode.POL);

		DokumentFil dokumentFilAfter = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilAfter.getFilUuid(), is(FIL_UUID_DUMMY_DOKUMENT_SKJERMET));
		assertThat(dokumentFilAfter.getFil(), is(TestDataGenerator.FIL_DUMMY_SKJERMET));
	}

	@Test
	public void shouldReturnDummyDokumentWhenKassert() {

		Journalpost journalpost = createJournalpostWithHoveddokument();

		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(createDummyDokumentKassert());
		journalpost = joarkRepository.save(journalpost);

		DokumentFil dokumentFilBefore = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilBefore.getFil(), is(arkivDokumentFil.getFil()));

		DokumentInfo hoveddok = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		hoveddok.setDatoKassert(LocalDateTime.now());
		hoveddok.setKassert(true);
		hoveddok.setKassertAvNavn("Navn");
		dokumentinfoRepository.save(hoveddok);

		DokumentFil dokumentFilAfter = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilAfter.getFil(), is(TestDataGenerator.FIL_DUMMY_KASSERT));
	}

	@Test
	public void shouldReturnDummySkjermetDokumentWhenSkjermet() {

		Journalpost journalpost = createJournalpostWithHoveddokument();

		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(createDummyDokumentKassert());
		journalpost = joarkRepository.save(journalpost);

		DokumentFil dokumentFilBefore = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilBefore.getFil(), is(arkivDokumentFil.getFil()));

		DokumentInfo hoveddok = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		hoveddok.setDatoKassert(LocalDateTime.now());
		hoveddok.setKassert(true);
		hoveddok.setKassertAvNavn("Navn");
		dokumentinfoRepository.save(hoveddok);

		DokumentFil dokumentFilAfter = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilAfter.getFil(), is(TestDataGenerator.FIL_DUMMY_KASSERT));
	}


	@Test
	public void shouldReturnSladdetForSladdetFilUuidVariantWhenArkivVariantIsSkjermet() {

		Journalpost journalpost = createJournalpostWithHoveddokument();

		DokumentInfo hoveddok = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		FilDetaljer arkiv = hoveddok.findFilDetaljerByVariantFormat(ARKIV);

		FilDetaljer sladdet = createFildetaljerOgFil(hoveddok, SLADDET);
		sladdet.setFileContent(FIL_SLADDET);

		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		DokumentFil sladdetDokumentFil = sladdet.createDokumentFil();

		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(sladdetDokumentFil);
		dokumentFilRepository.save(createDummyDokumentKassert());
		journalpost = joarkRepository.save(journalpost);

		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), ARKIV, SkjermingTypeCode.POL);

		DokumentFil dokumentFil = dokumentFilSkjermetRepository.findByFilUuid(sladdet.getFilUuid());
		assertThat(new String(dokumentFil.getFil()), is(new String(sladdetDokumentFil.getFil())));
	}

}