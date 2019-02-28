package no.nav.dokarkiv.journal.v3.tjoark058;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllScenarios;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.JANUARY_1_2020;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.createJournalpost;
import static no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider.PEN_SAK_ID;
import static no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider.createPENSaksrelasjon;
import static no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider.createSaksrelasjon;
import static no.nav.dokarkiv.core.domain.entities.DokumentInfo.DELETED_DOCUMENT_TITLE;
import static no.nav.dokarkiv.core.util.DateConverterUtil.convertDateToXMLGregorianCalendar;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.datautil.SkannetInnholdTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.journal.v3.AbstractJournalV3Itest;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeUgyldigInput;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Dokumentkategorier;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Dokumenttilstand;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journalposttyper;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Mottakskanaler;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Tema;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.ArkivSak;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.DetaljertDokumentinformasjon;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.DokumentInnhold;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.SkannetInnhold;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Soekefilter;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Integration test for HentKjerneJournalpostListe(TJOARK058) in 3rd gen. Journal
 * service.
 *
 * @author Stig Strøm
 */
public class HentKjerneJournalpostListeIT extends AbstractJournalV3Itest {

	private static final String FNR = "***gammelt_fnr***";
	private static final String ORG_NR = "220278387";
	private static final boolean DEFAULT_FEILREGISTRERT = false;
	private static final DokumentKategoriCode DOKUMENT_KATEGORI = DokumentKategoriCode.SOK;
	private static final FagsystemCode SAK_FAGSYSTEM = FagsystemCode.AO01;
	private static final String SAK_ID = "9999";
	private static final String DENY_PERMIT_ABAC_SCENARIO = "denypermitabac";

	@Before
	public void setUp() throws Exception {
		SubjectHandlerUtils.setInternBruker("userId");
	}

	@Test
	public void shouldThrowExceptionWhenFilteringGivesEmptySakList() throws Exception {
		abacDeny();

		joarkRepository.save(createJournalpost(DOKUMENT_KATEGORI).build());

		try {
			journalV3Provider.hentKjerneJournalpostListe(createRequest());
			fail();
		} catch (HentKjerneJournalpostListeSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), equalTo("Access Denied"));
		}

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/hentkjernejournalpost_1.json"))));
	}

	@Test
	public void shouldKeepAbacContextForEachCall() throws Exception {
		resetAllRequests();
		resetAllScenarios();
		stubFor(post("/abac")
				.inScenario(DENY_PERMIT_ABAC_SCENARIO)
				.willSetStateTo("permit")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
		stubFor(post("/abac")
				.inScenario(DENY_PERMIT_ABAC_SCENARIO)
				.whenScenarioStateIs("permit")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));


		HentKjerneJournalpostListeRequest request = createRequest();
		request.withArkivSakListe(new ArkivSak()
				.withArkivSakId(PEN_SAK_ID)
				.withArkivSakSystem(FagsystemCode.PEN.name()));

		journalV3Provider.hentKjerneJournalpostListe(request);

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/hentkjernejournalpost_2_1.json"))));
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/hentkjernejournalpost_2_2.json"))));
	}

	@Test
	public void shouldFilterSakListByAbac() throws Exception {
		resetAllRequests();
		resetAllScenarios();
		stubFor(post("/abac")
				.inScenario(DENY_PERMIT_ABAC_SCENARIO)
				.willSetStateTo("permit")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
		stubFor(post("/abac")
				.inScenario(DENY_PERMIT_ABAC_SCENARIO)
				.whenScenarioStateIs("permit")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));

		joarkRepository.save(createJournalpost(DOKUMENT_KATEGORI).build());

		Journalpost journalpostPen = joarkRepository.save(createJournalpost(DOKUMENT_KATEGORI)
				.saksrelasjon(createPENSaksrelasjon()).build());

		HentKjerneJournalpostListeRequest request = createRequest();
		request.withArkivSakListe(new ArkivSak()
				.withArkivSakId(PEN_SAK_ID)
				.withArkivSakSystem(FagsystemCode.PEN.name()));

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(request);

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.getJournalpostListe().get(0).getJournalpostId(), equalTo(String.valueOf(journalpostPen.getJournalpostId())));
	}

	@Test
	public void searchReturnsEmptyList() throws Exception {
		abacPermit();
		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response, is(notNullValue()));
		assertThat(response.getJournalpostListe(), empty());
	}

	@Test
	public void shouldReturnListWithOneJournalpost() throws Exception {
		abacPermit();
		Journalpost storedJournalpost = joarkRepository.save(createJournalpost(DOKUMENT_KATEGORI).build());

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.isSisteIntervall(), is(true));
		assertJournalpost(response.getJournalpostListe().get(0), storedJournalpost, Journaltilstand.ENDELIG, false);
	}

	@Test
	public void shouldReturnListWithOneJournalpostOnlyArkivVariantWhenKassert() throws Exception {
		abacPermit();
		Journalpost storedJournalpost = joarkRepository.save(createJournalpost(DOKUMENT_KATEGORI).build());
		skjermingService.skjermAllFildetaljer(storedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.isSisteIntervall(), is(true));
		assertThat(response.getJournalpostListe().get(0).getHoveddokument().getDokumentInnholdListe().size(), is(1));
		assertThat(response.getJournalpostListe().get(0).getHoveddokument().getDokumentInnholdListe().get(0).getVariantformat().getValue(), is("ARKIV"));
	}

	@Test
	public void shouldMapSamhandlerToPerson() throws Exception {
		abacPermit();
		joarkRepository.save(createJournalpost(DOKUMENT_KATEGORI).brukere(BrukerBuilder.getBrukerBuilder()
				.brukerId(FNR)
				.brukerType(BrukerTypeCode.SAMHANDLER)
				.opprettetKildeNavn("itest").build()).build());

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.isSisteIntervall(), is(true));
		assertThat(response.getJournalpostListe().get(0).getBrukerListe(), hasSize(1));
		assertThat(response.getJournalpostListe().get(0).getBrukerListe().get(0), instanceOf(Person.class));
		Person person = (Person) response.getJournalpostListe().get(0).getBrukerListe().get(0);
		assertThat(person.getIdent(), is(FNR));
	}

	@Test
	public void shouldMapSamhandlerToOrg() throws Exception {
		abacPermit();
		joarkRepository.save(createJournalpost(DOKUMENT_KATEGORI).brukere(BrukerBuilder.getBrukerBuilder()
				.brukerId(ORG_NR)
				.brukerType(BrukerTypeCode.SAMHANDLER)
				.opprettetKildeNavn("itest").build()).build());

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.isSisteIntervall(), is(true));
		assertThat(response.getJournalpostListe().get(0).getBrukerListe(), hasSize(1));
		assertThat(response.getJournalpostListe().get(0).getBrukerListe().get(0), instanceOf(Organisasjon.class));
		Organisasjon org = (Organisasjon) response.getJournalpostListe().get(0).getBrukerListe().get(0);
		assertThat(org.getOrgnr(), is(ORG_NR));
	}


	@Test
	public void shouldReturnJournalpostFeilregistrertJournaltilstandUtgaar() throws Exception {
		abacPermit();
		JournalpostBuilder journalpostBuilder = createJournalpost(DOKUMENT_KATEGORI);
		journalpostBuilder.saksrelasjon(createSaksrelasjon(true).build());
		Journalpost storedJournalpost = joarkRepository.save(journalpostBuilder.build());

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.isSisteIntervall(), is(true));
		assertJournalpost(response.getJournalpostListe().get(0), storedJournalpost, Journaltilstand.UTGAAR, true);
	}

	@Test
	public void shouldReturnJournaltilstandMidlertidig() throws Exception {
		JournalpostBuilder journalpostBuilder = createJournalpost(DOKUMENT_KATEGORI);
		journalpostBuilder.journalStatus(JournalStatusCode.OD);
		Journalpost storedJournalpost = joarkRepository.save(journalpostBuilder.build());

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.isSisteIntervall(), is(true));
		assertJournalpost(response.getJournalpostListe().get(0), storedJournalpost, Journaltilstand.MIDLERTIDIG, false);
	}

	@Test
	public void shouldReturnJournaltilstandUtgaar() throws Exception {
		abacPermit();
		JournalpostBuilder journalpostBuilder = createJournalpost(DOKUMENT_KATEGORI);
		journalpostBuilder.journalStatus(JournalStatusCode.U);
		Journalpost storedJournalpost = joarkRepository.save(journalpostBuilder.build());

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.isSisteIntervall(), is(true));
		assertJournalpost(response.getJournalpostListe().get(0), storedJournalpost, Journaltilstand.UTGAAR, false);
	}

	@Test
	public void shouldThrowUgyldigInputException_whenSakIdIsMissing() throws Exception {
		abacPermit();
		expectedException.expect(HentKjerneJournalpostListeUgyldigInput.class);
		expectedException.expectMessage("ArkivSakId er tom eller null");

		HentKjerneJournalpostListeRequest wsRequest = createRequest();
		wsRequest.getArkivSakListe().get(0).setArkivSakId("");
		journalV3Provider.hentKjerneJournalpostListe(wsRequest);
	}


	@Test
	public void shouldThrowUgyldigInputException_whenSakArkivSystemIsMissing() throws Exception {
		abacPermit();
		expectedException.expect(HentKjerneJournalpostListeUgyldigInput.class);
		expectedException.expectMessage("ArkivSakSystem er tom eller null");

		HentKjerneJournalpostListeRequest wsRequest = createRequest();
		wsRequest.getArkivSakListe().get(0).setArkivSakSystem("");
		journalV3Provider.hentKjerneJournalpostListe(wsRequest);
	}

	@Test
	public void shouldThrowIfJournalFomIsInTheFuture() throws Exception {
		abacPermit();
		Date futureDate = Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

		HentKjerneJournalpostListeRequest request = createRequest();
		request.withSoekefilter(new Soekefilter().withJournalFom(convertDateToXMLGregorianCalendar(futureDate)));


		expectedException.expect(HentKjerneJournalpostListeUgyldigInput.class);
		expectedException.expectMessage("Ugyldig datointervall. JournalFom er etter dagens dato.");
		journalV3Provider.hentKjerneJournalpostListe(request);
	}

	@Test
	public void shouldThrowIfJournalFomIsAfterJournalTom() throws Exception {
		abacPermit();
		Date lastYear = Date.from(LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date yesterday = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

		HentKjerneJournalpostListeRequest request = createRequest();
		request.withSoekefilter(new Soekefilter()
				.withJournalFom(convertDateToXMLGregorianCalendar(yesterday))
				.withJournalTom(convertDateToXMLGregorianCalendar(lastYear)));

		expectedException.expect(HentKjerneJournalpostListeUgyldigInput.class);
		expectedException.expectMessage("Ugyldig datointervall. JournalFom er etter journalTom.");

		journalV3Provider.hentKjerneJournalpostListe(request);
	}

	@Test
	public void shouldFailOnListeLargerThanPredefinertAntallSaker() throws Exception {
		abacPermit();
		expectedException.expect(HentKjerneJournalpostListeUgyldigInput.class);
		expectedException.expectMessage("Saksliste må begrenses");

		int predefinertAntall = 51;
		List<ArkivSak> arkivSaker = new ArrayList<>(predefinertAntall);
		for (int i = 0; i < predefinertAntall; i++) {
			arkivSaker.add(new ArkivSak().withArkivSakId(SAK_ID).withArkivSakSystem(SAK_FAGSYSTEM.name()));
		}
		HentKjerneJournalpostListeRequest request = createRequest();
		request.withArkivSakListe(arkivSaker);

		journalV3Provider.hentKjerneJournalpostListe(request);
	}

	@Test
	public void shouldNotReturnJournalpostWhenOpprettetDateIsBeforeSoekefilter() throws Exception {
		abacPermit();
		Date lastYear = Date.from(LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date yesterday = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

		joarkRepository.save(createJournalpost(FIL_UUID).build());

		HentKjerneJournalpostListeRequest request = createRequest();
		request.withSoekefilter(new Soekefilter()
				.withJournalFom(convertDateToXMLGregorianCalendar(lastYear))
				.withJournalTom(convertDateToXMLGregorianCalendar(yesterday)));

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(request);

		assertThat(response.getJournalpostListe(), is(empty()));
	}


	@Test
	public void shouldReturnIsSisteIntervallFalseWhenThereIsMoreJournalposts() throws Exception {
		abacPermit();
		for (int i = 0; i < 60; i++) {
			joarkRepository.save(createJournalpost(FIL_UUID).build());
		}

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(createRequest());

		assertThat(response.getJournalpostListe(), hasSize(50));
		assertThat(response.isSisteIntervall(), is(false));
	}

	@Test
	public void shouldIsSisteIntervallFalseWhenEndOfResultSet() throws Exception {
		abacPermit();
		for (int i = 0; i < 60; i++) {
			joarkRepository.save(createJournalpost(FIL_UUID).build());
		}

		HentKjerneJournalpostListeRequest requestWithResultatSettNr1 = createRequest();
		requestWithResultatSettNr1.setResultatSettNr(1);

		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(requestWithResultatSettNr1);

		assertThat(response.getJournalpostListe(), hasSize(10));
		assertThat(response.isSisteIntervall(), is(true));
	}

	@Test
	public void shouldFilterOnTemaPEN() throws Exception {
		abacPermit();
		joarkRepository.save(createJournalpost(FagomradeCode.PEN).build());
		joarkRepository.save(createJournalpost(FagomradeCode.AAP).build());

		HentKjerneJournalpostListeRequest request = createRequest();
		request.withSoekefilter(new Soekefilter().withTema(new Tema().withValue(FagomradeCode.PEN.name())));
		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(request);

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThat(response.getJournalpostListe().get(0).getTema().getValue(), is(FagomradeCode.PEN.name()));
		assertThat(response.isSisteIntervall(), is(true));
	}

	@Test
	public void shouldFilterOnJournalposttypeI() throws Exception {
		abacPermit();
		joarkRepository.save(createJournalpost(FagomradeCode.PEN).build());
		joarkRepository.save(createJournalpost(FagomradeCode.AAP).journalpostType(JournalpostTypeCode.I).build());
		joarkRepository.save(createJournalpost(FagomradeCode.AAP).journalpostType(JournalpostTypeCode.I).build());

		HentKjerneJournalpostListeRequest request = createRequest();
		request.withSoekefilter(new Soekefilter().withJournalposttype(new Journalposttyper().withValue(JournalpostTypeCode.I.name())));
		HentKjerneJournalpostListeResponse response = journalV3Provider.hentKjerneJournalpostListe(request);

		assertThat(response.getJournalpostListe(), hasSize(2));
		assertThat(response.isSisteIntervall(), is(true));

	}

	private void assertJournalpost(
			no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Journalpost wsJournalpost,
			Journalpost j, Journaltilstand journalTilstand, boolean feilregistrert, Dokumenttilstand dokTilstand,
			String dokumenttittel) {
		assertThat(wsJournalpost.getJournalpostId(), is(String.valueOf(j.getJournalpostId())));
		assertSak(wsJournalpost.getGjelderArkivSak(), feilregistrert);
		assertKorrespodansePart(wsJournalpost);
		assertThat(wsJournalpost.getKryssreferanseListe(), is(empty()));

		assertHoveddokument(wsJournalpost.getHoveddokument(), j.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), dokTilstand, dokumenttittel);
		assertThat(wsJournalpost.getVedleggListe(), is(empty()));
		assertThat(wsJournalpost.getBrukerListe(), hasSize(0));
		assertThat(wsJournalpost.getJournaltilstand(), is(journalTilstand));
		assertJournalposttype(wsJournalpost.getJournalposttype());
		assertTema(wsJournalpost.getTema());
		assertMottakskanal(wsJournalpost.getMottakskanal());
		assertThat(wsJournalpost.getInnhold(), is(JournalpostTestDataProvider.JP_INNHOLD));
		assertThat(wsJournalpost.getForsendelseJournalfoert(), is(convertDateToXMLGregorianCalendar(JANUARY_1_2020)));
		assertThat(wsJournalpost.getForsendelseMottatt(), is(convertDateToXMLGregorianCalendar(JANUARY_1_2020)));
		assertThat(wsJournalpost.getHoveddokument().getDokumentInnholdListe().size(), is(2));
	}

	private void assertJournalpost(
			no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Journalpost wsJournalpost,
			Journalpost j, Journaltilstand journalTilstand, boolean feilregistrert) {
		assertJournalpost(wsJournalpost, j, journalTilstand, feilregistrert, Dokumenttilstand.FERDIGSTILT, DokumentInfoTestDataProvider.DOKUMENT_TITTEL);
	}

	private HentKjerneJournalpostListeRequest createRequest() {
		return new HentKjerneJournalpostListeRequest()
				.withArkivSakListe(new ArkivSak().withArkivSakId(SAK_ID).withArkivSakSystem(SAK_FAGSYSTEM.name()))
				.withResultatSettNr(0)
				.withResultatSettStoerrelse(50);
	}

	private void assertSak(ArkivSak gjelderSak, boolean feilregistrert) {
		assertThat(gjelderSak.getArkivSakId(), is(SaksrelasjonTestDataProvider.SAK_ID));
		assertThat(gjelderSak.isErFeilregistrert(), is(feilregistrert));
		assertThat(gjelderSak.getArkivSakSystem(), is(SaksrelasjonTestDataProvider.SAK_FAGSYSTEM.name()));
	}

	private void assertKorrespodansePart(
			no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Journalpost journalpost) {
		assertThat(journalpost.getKorrespondansePart(), is(notNullValue()));
		assertThat(journalpost.getKorrespondansePart().getKorrespondansepartId(), is(JournalpostTestDataProvider.JP_AVSENDER_MOTTAKER_ID));
		assertThat(journalpost.getKorrespondansePart().getKorrespondansepartNavn(), is(JournalpostTestDataProvider.JP_AVSENDER_MOTTAKER));
		assertThat(journalpost.getKorrespondansePart().getKorrespondansepartType(), is("Mottaker"));
	}

	private void assertMottakskanal(Mottakskanaler mottakskanal) {
		assertThat(mottakskanal, is(notNullValue()));
		assertThat(mottakskanal.getValue(), is(MottaksKanalCode.NAV_NO.name()));
	}

	private void assertTema(Tema tema) {
		assertThat(tema, is(notNullValue()));
		assertThat(tema.getValue(), is(JournalpostTestDataProvider.JP_FAGOMRADE.name()));
	}

	private void assertJournalposttype(Journalposttyper journalposttype) {
		assertThat(journalposttype, is(notNullValue()));
		assertThat(journalposttype.getValue(), is(JournalpostTestDataProvider.JP_TYPE.name()));
	}

	private void assertDokumentkategori(Dokumentkategorier dokKategori) {
		assertThat(dokKategori, is(notNullValue()));
		assertThat(dokKategori.getValue(), is(DOKUMENT_KATEGORI.name()));
	}

	private void assertHoveddokument(DetaljertDokumentinformasjon dokument, DokumentInfo dokumentInfo, Dokumenttilstand dokTilstand, String dokumenttittel) {
		assertThat(dokument.getDokumentId(), is(String.valueOf(dokumentInfo.getDokumentInfoId())));
		assertThat(dokument.getDokumentInnholdListe(), hasSize(2));
		assertDokumentInnhold(dokument.getDokumentInnholdListe().get(0));
		if (!DELETED_DOCUMENT_TITLE.equals(dokumenttittel)) {
			assertDokumentkategori(dokument.getDokumentkategori());
		}
		assertThat(dokument.getTittel(), is(dokumenttittel));
		assertThat(dokument.getDokumenttilstand(), is(dokTilstand));
		assertSkannetInnholdListe(dokument.getSkannetInnholdListe(), 1);
	}

	private void assertDokumentInnhold(DokumentInnhold dokumentInnhold) {
		assertThat(dokumentInnhold.getArkivfiltype(), is(notNullValue()));
		assertThat(dokumentInnhold.getArkivfiltype().getValue(), is(FilTypeCode.PDF.name()));
		assertThat(dokumentInnhold.getVariantformat(), is(notNullValue()));
		assertThat(dokumentInnhold.getVariantformat()
				.getValue(), isIn(new String[]{VariantFormatCode.ARKIV.name(), VariantFormatCode.SLADDET.name()}));
	}

	private void assertSkannetInnholdListe(List<SkannetInnhold> skannetInnholdListe, int size) {
		assertThat(skannetInnholdListe, hasSize(size));
		assertThat(skannetInnholdListe.get(0).getDokumenttypeId().getValue(), is(SkannetInnholdTestDataProvider.DOKUMENT_TYPE_ID));
		assertThat(skannetInnholdListe.get(0).getVedleggInnhold(), is(SkannetInnholdTestDataProvider.VEDLEGG_INNHOLD));
	}
}
