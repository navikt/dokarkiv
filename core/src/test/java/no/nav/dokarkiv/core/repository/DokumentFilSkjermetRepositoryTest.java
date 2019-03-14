package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.FIL_NAVN;
import static no.nav.dokarkiv.core.util.TestDataGenerator.FIL_SLADDET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.OPPRETTET_KILDE_NAVN;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDummyDokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataGenerator;
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
import java.time.LocalDateTime;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, SkjermingService.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@Transactional
@ActiveProfiles("itest")
public class DokumentFilSkjermetRepositoryTest {


	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private DokumentinfoRepository dokumentinfoRepository;

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	private DokumentFilRepository dokumentFilRepository;

	@Inject
	private DokumentFilSkjermetRepository dokumentFilSkjermetRepository;

	@Inject
	private SkjermingService skjermingService;

	@Before
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@After
	public void cleanUp() {
		TestTransaction.end();
		dokumentFilRepository.deleteAll();
		journalpostDokumentInfoRelasjonRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		joarkRepository.deleteAll();

	}

	@Test
	public void shouldReturnDummyDokumentWhenArkivVariantIsSkjermet(){

		Journalpost journalpost = createJournalpostWithHoveddokument();

		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(createDummyDokument());
		journalpost = joarkRepository.save(journalpost);

		DokumentFil dokumentFilBefore = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilBefore.getFil(), is(arkivDokumentFil.getFil()));

		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), ARKIV, SkjermingTypeCode.POL);

		DokumentFil dokumentFilAfter = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilAfter.getFil(), is(TestDataGenerator.FIL_DUMMY));
	}

	@Test
	public void shouldReturnDummyDokumentWhenKassert(){

		Journalpost journalpost = createJournalpostWithHoveddokument();

		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(createDummyDokument());
		journalpost = joarkRepository.save(journalpost);

		DokumentFil dokumentFilBefore = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilBefore.getFil(), is(arkivDokumentFil.getFil()));

		DokumentInfo hoveddok = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		hoveddok.setDatoKassert(LocalDateTime.now());
		hoveddok.setKassertAvNavn("Navn");
		dokumentinfoRepository.save(hoveddok);

		DokumentFil dokumentFilAfter = dokumentFilSkjermetRepository.findByFilUuid(arkiv.getFilUuid());
		assertThat(dokumentFilAfter.getFil(), is(TestDataGenerator.FIL_DUMMY));
	}

	@Test
	public void shouldReturnDummyDokumentWhenFilUuidContainsDummyDokument() {
		Journalpost journalpost = createJournalpostWithHoveddokument();

		FilDetaljer arkiv = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(createDummyDokument());
		journalpost = joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		DokumentFil dokumentFilAfter = dokumentFilSkjermetRepository.findByFilUuid("ADADS_DUMMY_DOKUMENT_KASSERT_AASDSAD");
		assertThat(dokumentFilAfter.getFil(), is(TestDataGenerator.FIL_DUMMY));
	}

	@Test
	public void shouldReturnSladdetForSladdetFilUuidVariantWhenArkivVariantIsSkjermet(){

		Journalpost journalpost = createJournalpostWithHoveddokument();

		DokumentInfo hoveddok = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		FilDetaljer arkiv = hoveddok.findFilDetaljerByVariantFormat(ARKIV);

		FilDetaljer sladdet = createFildetaljerOgFil(hoveddok, SLADDET);
		sladdet.setFileContent(FIL_SLADDET);

		DokumentFil arkivDokumentFil = arkiv.createDokumentFil();
		DokumentFil sladdetDokumentFil = sladdet.createDokumentFil();

		dokumentFilRepository.save(arkivDokumentFil);
		dokumentFilRepository.save(sladdetDokumentFil);
		dokumentFilRepository.save(createDummyDokument());
		journalpost = joarkRepository.save(journalpost);

		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), ARKIV, SkjermingTypeCode.POL);

		DokumentFil dokumentFil = dokumentFilSkjermetRepository.findByFilUuid(sladdet.getFilUuid());
		assertThat(new String(dokumentFil.getFil()), is(new String(sladdetDokumentFil.getFil())));
	}

}