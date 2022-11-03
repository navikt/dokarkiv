package no.nav.dokarkiv.hentdokument;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentUrlInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentUrlInfoRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.assertj.core.util.DateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static no.nav.dokarkiv.core.repository.DokumentFilSkjermetRepository.FIL_UUID_DUMMY_DOKUMENT_KASSERT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, HentDokumentConfig.class, TokenGeneratorConfiguration.class})
@ActiveProfiles({"itest", "wiremock"})
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureWireMock(port = 0)
@Transactional
public class HentDokumentControllerIT {

	private static final String FIL_UUID = FilDetaljer.generateUuid();
	private static final String FIL_UUID_SLADDET = FilDetaljer.generateUuid();
	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private static final byte[] FIL_CONTENT = "Test".getBytes();
	private static final byte[] FIL_CONTENT_DUMMY = "Dummy".getBytes();
	private static final byte[] FIL_CONTENT_SKJERMET = "Skjermet".getBytes();

	private static final OnDemandInstansCode ON_DEMAND_INSTANS = OnDemandInstansCode.PESYS;
	private static final String ON_DEMAND_ID = "onDemandId";
	private static final byte[] ONDEMAND_FIL_CONTENT = "e-business".getBytes();

	@Autowired
	private DokumentUrlInfoRepository dokumentUrlInfoRepository;
	@Autowired
	private JoarkRepositorySkjermet joarkRepository;
	@Autowired
	private DokumentFilRepository dokumentFilRepository;
	@Autowired
	private JournalpostDokumentInfoRelasjonRepository relasjonRepository;
	@Autowired
	private DokumentinfoRepository dokumentinfoRepository;
	@Autowired
	private TestRestTemplate testRestTemplate;
	@Autowired
	private SkjermingServiceTest skjermingService;
	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	public void setUp() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	@AfterEach
	public void deleteAll() {
		dokumentUrlInfoRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		relasjonRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		joarkRepository.deleteAll();
		if (entityManager.isJoinedToTransaction()) {
			entityManager.flush();
			entityManager.clear();
		}

	}

	@Test
	public void shouldFetchOnDemandDocumentFromJoarkInsteadOfDatabase() {
		String docToken = UUID.randomUUID().toString();
		Journalpost journalpost = joarkRepository.save(createOnDemandJournalpostBuilder().build());
		dokumentUrlInfoRepository.save(createDokumentUrlInfo(journalpost, docToken, FIL_UUID).build());
		TestTransaction.flagForCommit();
		TestTransaction.end();

		stubFor(get(urlMatching("\\/joarkhentdokument\\?docToken=[a-zA-Z0-9\\-]+&mimetype=application%252Fpdf"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
						.withBody(ONDEMAND_FIL_CONTENT)));

		ResponseEntity<byte[]> response = testRestTemplate.getForEntity("/hentdokument?docToken=" + docToken + "&amp;mimetype=application%2Fpdf", byte[].class);

		assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
		assertArrayEquals(ONDEMAND_FIL_CONTENT, response.getBody());
	}

	@Test
	public void shouldFetchDocumentFromDatabase() {
		String docToken = UUID.randomUUID().toString();
		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder("tittel").build());
		dokumentUrlInfoRepository.save(createDokumentUrlInfo(journalpost, docToken, FIL_UUID).build());
		persistDokumentFil(FIL_CONTENT);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<byte[]> response = testRestTemplate.getForEntity("/hentdokument?docToken=" + docToken + "&amp;mimetype=application%2Fpdf", byte[].class);

		assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
		assertArrayEquals(FIL_CONTENT, response.getBody());
	}

	@Test
	public void shouldFetchDummyDokumentWhenDokumentIsKassert() {
		String docToken = UUID.randomUUID().toString();
		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder("tittel").build());
		dokumentUrlInfoRepository.save(createDokumentUrlInfo(journalpost, docToken, FIL_UUID).build());
		persistDokumentFil(FIL_CONTENT);
		skjermingService.setDokumentKassert(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<byte[]> response = testRestTemplate.getForEntity("/hentdokument?docToken=" + docToken + "&amp;mimetype=application%2Fpdf", byte[].class);

		assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
		assertArrayEquals(FIL_CONTENT_DUMMY, response.getBody());
	}

	@Test
	public void shouldFetchSkjermetDokumentWhenArkivVariantIsSkjermet() {
		String docToken = UUID.randomUUID().toString();
		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder("tittel").build());
		dokumentUrlInfoRepository.save(createDokumentUrlInfo(journalpost, docToken, FIL_UUID).build());
		persistDokumentFil(FIL_CONTENT);
		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo().getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<byte[]> response = testRestTemplate.getForEntity("/hentdokument?docToken=" + docToken + "&amp;mimetype=application%2Fpdf", byte[].class);

		assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
		assertArrayEquals(FIL_CONTENT_SKJERMET, response.getBody());
	}

	@Test
	public void shouldThrowWhenJournalpostIsSkjermet() {
		String docToken = UUID.randomUUID().toString();
		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder("tittel").build());
		dokumentUrlInfoRepository.save(createDokumentUrlInfo(journalpost, docToken, FIL_UUID).build());
		persistDokumentFil(FIL_CONTENT);
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> response = testRestTemplate.getForEntity("/hentdokument?docToken=" + docToken + "&amp;mimetype=application%2Fpdf", String.class);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
		assertThat(response.getBody(), is(String.format("Journalpost med id %s eksisterer ikke", journalpost.getJournalpostId())));

	}

	@Test
	public void shouldFetchAndUpdateDlfDocument() throws IOException {
		String docToken = UUID.randomUUID().toString();

		Journalpost journalpost = createJournalpostBuilder("tittel").build();
		journalpost.findAllFilDetaljer().get(0).setVariantFormat(VariantFormatCode.PRODUKSJON_DLF);
		joarkRepository.save(journalpost);
		dokumentUrlInfoRepository.save(createDokumentUrlInfo(journalpost, docToken, FIL_UUID).build());
		persistDokumentFil(byteArrFromClasspath("EESSI.dlf"));
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<byte[]> response = testRestTemplate.getForEntity("/hentdokument?docToken=" + docToken + "&amp;mimetype=application%2Fpdf", byte[].class);

		assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
	}

	@Test
	public void shouldThrowExceptionInvalidDocToken() {
		String docToken = UUID.randomUUID().toString();

		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder("tittel").build());
		dokumentUrlInfoRepository.save(createDokumentUrlInfo(journalpost, docToken, FIL_UUID).build());
		persistDokumentFil(FIL_CONTENT);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<byte[]> response = testRestTemplate.getForEntity("/hentdokument?docToken=xyz&amp;mimetype=application%2Fpdf", byte[].class);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
	}

	private JournalpostBuilder createJournalpostBuilder(String dokumentTittel) {
		return JournalpostBuilder
				.getJournalpostBuilder()
				.journalStatus(JournalStatusCode.FS)
				.fagomrade(FagomradeCode.FOR)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.brukere(BrukerTestDataProvider.createBruker().build())
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder
								.getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetAvNavn("testuser")
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.opprettetKildeNavn("test")
								.dokumentInfo(
										DokumentInfoBuilder
												.getDokumentInfoBuilder()
												.opprettetKildeNavn("test")
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.tittel(dokumentTittel)
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.filUuid(FIL_UUID).variantFormat(VARIANT_FORMAT)
																.opprettetKildeNavn("test").build(),
														FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.filUuid(FIL_UUID_SLADDET).variantFormat(VariantFormatCode.SLADDET)
																.opprettetKildeNavn("test").build()).build())
								.build());
	}

	private JournalpostBuilder createOnDemandJournalpostBuilder() {
		return JournalpostBuilder
				.getJournalpostBuilder()
				.journalStatus(JournalStatusCode.FS)
				.fagomrade(FagomradeCode.FOR)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.brukere(BrukerTestDataProvider.createBruker().build())
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder
								.getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetAvNavn("testuser")
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.opprettetKildeNavn("test")
								.dokumentInfo(
										DokumentInfoBuilder
												.getDokumentInfoBuilder()
												.opprettetKildeNavn("test")
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.tittel("Dokumentittel")
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.filUuid(FIL_UUID).variantFormat(VARIANT_FORMAT)
																.onDemandId(ON_DEMAND_ID).onDemandInstans(ON_DEMAND_INSTANS)
																.opprettetKildeNavn("test").build()).build()).build());
	}

	private DokumentUrlInfoBuilder createDokumentUrlInfo(Journalpost journalpost, String docToken, String filUuid) {
		return DokumentUrlInfoBuilder.getDokumentUrlInfoBuilder()
				.docToken(docToken)
				.tidspunkt(DateUtil.now())
				.timeToLiveMinutes(5L)
				.journalpost(journalpost)
				.filUuid(filUuid);
	}

	private void persistDokumentFil(byte[] fileContent) {
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID)
				.fil(fileContent)
				.opprettetKildeNavn("test")
				.build());
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID_DUMMY_DOKUMENT_KASSERT)
				.fil(FIL_CONTENT_DUMMY)
				.opprettetKildeNavn("test")
				.build());
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID_SLADDET)
				.fil(FIL_CONTENT_SKJERMET)
				.opprettetKildeNavn("test")
				.build());
	}

	protected byte[] byteArrFromClasspath(String resourcename) throws IOException {
		return IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}
}
