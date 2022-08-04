package no.nav.dokarkiv.journal.v3.tjoark050;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
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
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.journal.v3.AbstractJournalV3Itest;
import no.nav.modig.core.domain.IdentType;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.transaction.TestTransaction;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokarkiv.core.repository.DokumentFilSkjermetRepository.FIL_UUID_DUMMY_DOKUMENT_KASSERT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration test for HentDokumentURL in 3rd gen. Journal service.
 *
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class HentDokumentURLIT extends AbstractJournalV3Itest {

	private static final String FIL_UUID = FilDetaljer.generateUuid();
	private static final String FIL_UUID_SLADDET = FilDetaljer.generateUuid();
	private static final String FIL_UUID_DUMMY = FilDetaljer.generateUuid();

	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private String journalpostId;
	private String dokumentInfoId;
	private Journalpost journalpost;
	private HentDokumentURLRequest request = new HentDokumentURLRequest();

	@BeforeEach
	public void setUp() {
		journalpost = buildAndPersistJournalpost("Dokumenttittel");
		createRequestFromJournalpost(journalpost);

		SubjectHandlerUtils.setSubject(new SubjectHandlerUtils.SubjectBuilder(INTERN_BRUKER_USER_ID, IdentType.InternBruker).getSubject());
	}


	@Test
	public void shouldFailWhenABACDenies() throws Exception {
		abacDeny();

		persistDokumentFil();

		try {
			journalV3Provider.hentDokumentURL(request);
			fail();
		} catch (HentDokumentURLSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), equalTo("Bruker har ikke tilgang til journalpost"));
		}

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/hentdokumenturl.json"))));
	}

	@Test
	public void shouldAllowAccessWhenAbacPermits() throws Exception {
		abacPermit();

		persistDokumentFil();

		journalV3Provider.hentDokumentURL(request);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostNotFound() throws Exception {
		abacPermit();
		request.setJournalpostId("123");

		assertThrows(HentDokumentURLDokumentIkkeFunnet.class,
				() -> journalV3Provider.hentDokumentURL(request),
				"Journalpost ikke funnet. journalpostId=123");
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() throws Exception {
		abacPermit();
		request.setDokumentId("123");

		assertThrows(HentDokumentURLDokumentIkkeFunnet.class,
				() -> journalV3Provider.hentDokumentURL(request),
				"Could not find document");
	}

	@Test
	public void shouldThrowExceptionWhenFilDetaljerNotFoundWithGivenVariant() throws Exception {
		abacPermit();
		Variantformater variantFormat = new Variantformater();
		variantFormat.setValue(VariantFormatCode.PRODUKSJON.name());
		request.setVariantformat(variantFormat);

		assertThrows(HentDokumentURLDokumentIkkeFunnet.class,
				() -> journalV3Provider.hentDokumentURL(request),
				"Could not find document");
	}

	@Test
	public void shouldThrowExceptionWhenDokumentFilNotFound() throws Exception {
		abacPermit();

		assertThrows(HentDokumentURLDokumentIkkeFunnet.class,
				() -> journalV3Provider.hentDokumentURL(request),
				"Could not find document");
	}

	@Test
	public void shouldGetDokumentUrl() throws Exception {
		abacPermit();
		persistDokumentFil();

		HentDokumentURLResponse response = journalV3Provider.hentDokumentURL(request);

		assertThat(response.getDokumentURL(), containsString("docToken"));
		assertDokumentUrlInfoIsPersisted(FIL_UUID);
	}

	@Test
	public void shouldThrowWhenJournalpostIsSkjermet() throws Exception {
		abacPermit();
		persistDokumentFil();

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThrows(HentDokumentURLDokumentIkkeFunnet.class,
				() -> journalV3Provider.hentDokumentURL(request),
				"Journalpost ikke funnet");
	}

	@Test
	public void shouldGetSladdetDokumentUrl() throws Exception {
		abacPermit();
		persistDokumentFil();

		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HentDokumentURLResponse response = journalV3Provider.hentDokumentURL(request);

		assertThat(response.getDokumentURL(), containsString("docToken"));
		assertDokumentUrlInfoIsPersisted(FIL_UUID_SLADDET);
	}

	@Test
	public void shouldReturnFilUuidForArkivVariantWhenDokumentIsKassert() throws Exception {
		abacPermit();
		persistDokumentFil();

		skjermingService.setDokumentKassert(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HentDokumentURLResponse response = journalV3Provider.hentDokumentURL(request);

		assertThat(response.getDokumentURL(), containsString("docToken"));
		assertDokumentUrlInfoIsPersisted(FIL_UUID);
	}

	private HentDokumentURLRequest createRequest() {
		request.setJournalpostId(journalpostId);
		request.setDokumentId(dokumentInfoId);
		Variantformater variantFormat = new Variantformater();
		variantFormat.setValue(VARIANT_FORMAT.name());
		request.setVariantformat(variantFormat);
		return request;
	}

	private void assertDokumentUrlInfoIsPersisted(String filuuid) {
		DokumentUrlInfo dokUrlInfo = dokumentUrlInfoRepository.findByFilUuid(filuuid);
		assertThat(dokUrlInfo.getDoctoken(), notNullValue());
		assertThat(dokUrlInfo.getFilUuid(), is(filuuid));
	}

	private void createRequestFromJournalpost(Journalpost journalpost) { // hentet fra HentDokumentTest
		journalpostId = journalpost.getId().toString();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId().toString();
		createRequest();
	}

	private Journalpost buildAndPersistJournalpost(String dokumentTittel) { // hentet fra HentDokumentTest
		return joarkRepository.save(createJournalpostBuilder(dokumentTittel)
				.fagomrade(FagomradeCode.FOR).build());
	}


	private JournalpostBuilder createJournalpostBuilder(String dokumentTittel) { // hentet fra HentDokumentTest
		return JournalpostBuilder
				.getJournalpostBuilder()
				.journalStatus(JournalStatusCode.FS)
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
																.opprettetKildeNavn("test").build()).build()).build());
	}

	private void persistDokumentFil() {
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID)
				.fil("Test".getBytes())
				.opprettetKildeNavn("test")
				.build());
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID_SLADDET)
				.fil("Test".getBytes())
				.opprettetKildeNavn("test")
				.build());
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID_DUMMY_DOKUMENT_KASSERT)
				.fil("Dummy".getBytes())
				.opprettetKildeNavn("test")
				.build());
	}

}
