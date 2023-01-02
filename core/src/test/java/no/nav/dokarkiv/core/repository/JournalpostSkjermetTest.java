package no.nav.dokarkiv.core.repository;

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

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class, SkjermingServiceTest.class, JdbcAbacSecurityRepository.class})
@ActiveProfiles("itest")
public class JournalpostSkjermetTest {

	@Autowired
	private JournalpostRepositorySkjermet journalpostRepositorySkjermet;

	@Autowired
	private JournalpostRepository journalpostRepository;

	@Autowired
	private DokumentInfoTestRepository dokumentinfoTestRepository;

	@Autowired
	private JournalpostDokumentInfoRelasjonTestRepository journalpostDokumentInfoRelasjonTestRepository;

	@Autowired
	private SkjermingService skjermingService;

	@Autowired
	private SkjermingServiceTest skjermingServiceTest;

	@BeforeEach
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@AfterEach
	public void cleanUp() {
		TestTransaction.end();
		journalpostDokumentInfoRelasjonTestRepository.deleteAll();
		dokumentinfoTestRepository.deleteAll();
		journalpostRepository.deleteAll();
	}

	@Test
	public void shouldNotReturnSkjermetJournalpostDokumentInfoRelasjons() {

		Journalpost journalpost1 = journalpostRepository.save(createJournalpostWithTwoVedlegg());
		JournalpostDokumentInfoRelasjon skjermetJournalpostDokumentInfoRelasjon = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next();
		Long skjermetDokumentInfoId = skjermetJournalpostDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId();
		Long skjermetDokumentFildetaljId = journalpost1.findDokumentInfoById(skjermetDokumentInfoId)
				.getFildetaljerListe()
				.iterator()
				.next()
				.getFildetaljerId();

		skjermingServiceTest.setJpDokInfoRelSkjerming(skjermetJournalpostDokumentInfoRelasjon.getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		//Test behaviour when skjermet
		Journalpost journalpostWithBegrensning = journalpostRepositorySkjermet.findById(journalpost1.getJournalpostId()).get();

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

		Journalpost journalpost2 = journalpostRepository.save(createJournalpostWithTwoVedlegg());
		Journalpost journalpostWithoutBegrensning = journalpostRepository.findById(journalpost2.getJournalpostId()).get();

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
