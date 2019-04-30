package no.nav.dokarkiv.core.repository;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, SkjermingService.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@ActiveProfiles("itest")
public class JournalpostSkjermetTest {

    @Inject
    private JoarkRepositorySkjermet joarkRepositorySkjermet;

    @Inject
    private JoarkRepository joarkRepository;

	@Inject
	private DokumentinfoRepository dokumentinfoRepository;

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	private SkjermingService skjermingService;

	@Before
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@After
	public void cleanUp() {
		TestTransaction.end();
		journalpostDokumentInfoRelasjonRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		joarkRepository.deleteAll();
	}

	@Test
	public void shouldNotReturnSkjermetJournalpostDokumentInfoRelasjons() {

		Journalpost journalpost1 = joarkRepository.save(createJournalpostWithTwoVedlegg());
		JournalpostDokumentInfoRelasjon skjermetJournalpostDokumentInfoRelasjon = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next();
		Long skjermetDokumentInfoId = skjermetJournalpostDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId();
		Long skjermetDokumentFildetaljId = journalpost1.findDokumentInfoById(skjermetDokumentInfoId)
				.getFildetaljerListe()
				.iterator()
				.next()
				.getFildetaljerId();

		skjermingService.setJpDokInfoRelSkjerming(skjermetJournalpostDokumentInfoRelasjon.getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		//Test behaviour when skjermet
		Journalpost journalpostWithBegrensning = joarkRepositorySkjermet.findById(journalpost1.getJournalpostId()).get();

		assertThat(journalpostWithBegrensning.getJournalpostDokumentInfoRelasjoner().size(), is(2));
		assertThat(journalpostWithBegrensning.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.anyMatch(rel -> rel.getDokumentInfo().getDokumentInfoId().equals(skjermetDokumentInfoId)), is(false));
		assertThat(journalpostWithBegrensning.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(skjermetDokumentInfoId), nullValue());
		assertThat(journalpostWithBegrensning.findDokumentInfoById(skjermetDokumentInfoId), nullValue());
		assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.size(), is(1));
		assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.stream()
				.anyMatch(rel -> rel.getDokumentInfo().getDokumentInfoId().equals(skjermetDokumentInfoId)), is(false));
		assertThat(journalpostWithBegrensning.findAllFilDetaljer().size(), is(2));
		assertThat(journalpostWithBegrensning.findAllFilDetaljer()
				.stream()
				.anyMatch(detalj -> detalj.getDokumentInfo().getDokumentInfoId().equals(skjermetDokumentInfoId)), is(false));
		assertThat(journalpostWithBegrensning.findFilDetaljerByFilDetaljerId(skjermetDokumentFildetaljId), nullValue());
		assertThat(journalpostWithBegrensning.findDokumentInfoRelasjonById(skjermetJournalpostDokumentInfoRelasjon.getJournalpostDokumentInfoRelasjonId()), nullValue());
		assertThat(journalpostWithBegrensning.findAllDokumentInfos().size(), is(2));

		Journalpost journalpost2 = joarkRepository.save(createJournalpostWithTwoVedlegg());
		Journalpost journalpostWithoutBegrensning = joarkRepository.findById(journalpost2.getJournalpostId()).get();

		assertThat(journalpostWithoutBegrensning.getJournalpostDokumentInfoRelasjoner().size(), is(3));
		assertThat(journalpostWithoutBegrensning.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(skjermetDokumentInfoId), nullValue());
		assertThat(journalpostWithoutBegrensning.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.size(), is(2));
		assertThat(journalpostWithoutBegrensning.findAllFilDetaljer().size(), is(3));
		assertThat(journalpostWithoutBegrensning.findAllDokumentInfos().size(), is(3));

	}

	private Journalpost createJournalpostWithTwoVedlegg() {
		Journalpost journalpost = createJournalpost();

		journalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.opprettetKildeNavn("test")
				.tilknyttetAvNavn("test")
				.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.opprettetKildeNavn("test")
						.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
								.filtype(FilTypeCode.PDF)
								.filUuid("uuid")
								.variantFormat(VariantFormatCode.PRODUKSJON)
								.opprettetKildeNavn("test")
								.build()
						)
						.build()).build());

		journalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.opprettetKildeNavn("test")
				.tilknyttetAvNavn("test")
				.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.opprettetKildeNavn("test")
						.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
								.filtype(FilTypeCode.PDF)
								.filUuid("uuid")
								.variantFormat(VariantFormatCode.PRODUKSJON)
								.opprettetKildeNavn("test")
								.build()
						)
						.build()).build());


		return journalpost;
	}

}
