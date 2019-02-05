package no.nav.dokarkiv.skjermarkivenhet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.security.JwtClaimsBuilderProvider.openAmClaimsBuilder;
import static no.nav.dokarkiv.core.util.ConverterUtils.objectToJsonString;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggRequest;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeaderMapper;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, SkjermArkivenhetConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataLdap
@AutoConfigureWireMock(port = 0)
@Transactional
public abstract class AbstractSkjermArkivenhetIT {

	protected static final String URL_SKJERMARKIVENHET = "/rest/skjermarkivenhet/";

	private static final String BEARER = "Bearer ";
	private static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String SERVICE_USER_ID = "srvjoarkadmin";
	private static final String PERSON_USER_ID = "Z990782";
	private static final String NO_ACCESS_SERVICE_USER_ID = "srvdokarkiv";
	private static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Endret av navn";
	private static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	private static final String BREVGRUPPE = "Brevgruppe";
	private static final String BREVKODE = "Brevkode";
	private static final String FILNAVN = "filNavn";
	private static final String TITTEL = "Tittel";
	private static Long journalpostId = 2000000L;
	private static Long jpDokInfoRelasjonId = 2000000L;
	private static Long dokumentInfoId = 2000000L;
	private String oidcTokenPersonUserTest;
	private String oidcTokenServiceUserTest;
	private String oidcTokenServiceNoAccessUserTest;


	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Inject
	protected OidcTestService oidcTestService;
	@Inject
	protected TestRestTemplate restTemplate;
	@Inject
	protected SkjermingService skjermingService;
	@Inject
	protected AksjonsLoggRepository aksjonsLoggRepository;
	@Inject
	protected AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper;
	@Inject
	protected JoarkRepository joarkRepository;

	@Before
	public void setUp() {
		oidcTokenPersonUserTest = BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
				.build());
		oidcTokenServiceUserTest = BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build());
		oidcTokenServiceNoAccessUserTest = BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(NO_ACCESS_SERVICE_USER_ID)
				.build());
	}

	@BeforeClass
	public static void setupItest() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	@Before
	public void cleanup() {
		joarkRepository.deleteAll();
		aksjonsLoggRepository.deleteAll();
	}

	protected HttpEntity createHeadersWithAksjon(String aksjon) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, oidcTokenPersonUserTest);
		headers.add(NAV_CONSUMER_TOKEN, oidcTokenServiceUserTest);
		headers.add(AksjonsLoggService.AKSJONS_LOGG_HEADER, objectToJsonString(createAksjonsLoggRequest(getJournalpostId(), getDokumentInfoId(), aksjon)));
		return new HttpEntity<>(headers);
	}

	protected HttpEntity createNoAccessHeadersWithAksjon(String aksjon) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, oidcTokenPersonUserTest);
		headers.add(NAV_CONSUMER_TOKEN, oidcTokenServiceNoAccessUserTest);
		headers.add(AksjonsLoggService.AKSJONS_LOGG_HEADER, objectToJsonString(createAksjonsLoggRequest(getJournalpostId(), getDokumentInfoId(), aksjon)));
		return new HttpEntity<>(headers);
	}

	public static Long getJournalpostId() {
		return journalpostId;
	}

	public static Long getDokumentInfoId() {
		return dokumentInfoId;
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	public static Journalpost opprettHoveddokumentForIT() {
		return getBaseJournalpostBuilder()
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getBaseDokumentInfoBuilder().build())
								.build())
				.build();
	}

	public static Journalpost opprettHoveddokumentForEnhetstest() {
		return getBaseJournalpostBuilder()
				.journalpostId(journalpostId++)
				.dokumentInfoRelasjoner(
						getBaseJournalpostDokumentInfoRelasjonBuilder()
								.journalpostDokumentInfoRelasjonId(jpDokInfoRelasjonId++)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getBaseDokumentInfoBuilder()
										.dokumentInfoId(dokumentInfoId)
										.build())
								.build())
				.build();
	}


	public static Journalpost opprettHoveddokumentMedEtKnyttetVedleggForIT() {
		Journalpost journalpost = opprettHoveddokumentForIT();
		journalpost.addJournalpostDokumentInfoRelasjon(getBaseJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.dokumentInfo(getBaseDokumentInfoBuilder()
						.originalJournalpost(journalpost)
						.build())
				.build());
		return journalpost;
	}

	public static void knyttDokumentInfoSomVedleggTilJournalpostForIT(DokumentInfo dokInfoVedlegg, Journalpost jpHovedokument) {
		jpHovedokument.addJournalpostDokumentInfoRelasjon(
				getBaseJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.dokumentInfo(dokInfoVedlegg)
						.build());
	}

	private static JournalpostBuilder getBaseJournalpostBuilder() {
		return JournalpostBuilder.getJournalpostBuilder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.dokumentDato(new Date())
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalStatus(JournalStatusCode.FS)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.addOriginalJournalpost(true)
				.fagomrade(FagomradeCode.RPO)
				.saksrelasjon(
						SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.brukere(
						BrukerTestDataProvider.createBruker().build())
				.mottakskanal(MottaksKanalCode.NAV_NO);
	}

	private static JournalpostDokumentInfoRelasjonBuilder getBaseJournalpostDokumentInfoRelasjonBuilder() {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN);
	}

	private static DokumentInfoBuilder getBaseDokumentInfoBuilder() {
		return DokumentInfoBuilder.getDokumentInfoBuilder()
				.tittel(TITTEL)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.endretAvNavn(ENDRET_AV_NAVN)
				.brevgruppe(BREVGRUPPE)
				.brevkode(BREVKODE)
				.filDetaljerList(createFildetaljer())
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN);
	}

	private static FilDetaljer createFildetaljer() {
		return createFildetaljer(FilDetaljer.generateUuid());
	}

	private static FilDetaljer createFildetaljer(String filUuid) {
		return FilDetaljerBuilder.getFilDetaljerBuilder()
				.filUuid(filUuid)
				.filnavn(FILNAVN)
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ARKIV)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fileContent("ARKIV variant".getBytes())
				.build();
	}
}
