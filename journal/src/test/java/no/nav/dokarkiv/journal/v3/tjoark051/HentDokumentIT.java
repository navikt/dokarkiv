package no.nav.dokarkiv.journal.v3.tjoark051;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokarkiv.core.domain.entities.DokumentInfo.DELETED_DOCUMENT_TITLE;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.journal.v3.AbstractJournalV3Itest;
import no.nav.modig.core.domain.IdentType;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.feil.DokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentResponse;
import org.apache.http.HttpHeaders;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for HentDokument(TJOARK051) in 3rd gen. Journal service.
 *
 * @author Stig Strøm (Copied by Roar Bjurstrøm)
 */
public class HentDokumentIT extends AbstractJournalV3Itest {

	private static final String FIL_UUID = FilDetaljer.generateUuid();
	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private static final byte[] FIL_CONTENT = "Test".getBytes();

	private static final String FIL_UUID_SLADDET = FilDetaljer.generateUuid();
	private static final VariantFormatCode VARIANT_FORMAT_SLADDET = VariantFormatCode.SLADDET;
	private static final byte[] FIL_CONTENT_SLADDET = "sladdet".getBytes();


	private static final OnDemandInstansCode ON_DEMAND_INSTANS = OnDemandInstansCode.PESYS;
	private static final String ON_DEMAND_ID = "onDemandId";
	private static final byte[] ONDEMAND_FIL_CONTENT = "e-business".getBytes();

	@Before
	public void setUp() {
		SubjectHandlerUtils.setSubject(new SubjectHandlerUtils.SubjectBuilder(INTERN_BRUKER_USER_ID, IdentType.InternBruker).getSubject());
	}

	@Test
	public void shouldFailWhenABACDenies() throws Exception {
		abacDeny();

		Journalpost journalpost = buildAndPersistJournalpost("Dokumenttittel");
		HentDokumentRequest request = createRequest(journalpost);
		persistDokumentFil();

		try {
			journalV3Provider.hentDokument(request);
			fail();
		} catch (HentDokumentSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), CoreMatchers.equalTo("Bruker har ikke tilgang til journalpost"));
		}

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/hentdokument.json"))));
	}

	@Test
	public void shouldAllowAccessWhenAbacPermits() throws Exception {
		abacPermit();

		Journalpost journalpost = buildAndPersistJournalpost("Dokumenttittel");
		HentDokumentRequest request = createRequest(journalpost);
		persistDokumentFil();

		journalV3Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostNotFound() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndPersistJournalpost("Dokumenttittel");
		HentDokumentRequest request = createRequest(journalpost);
		request.setJournalpostId("123");

		expectedException.expect(HentDokumentDokumentIkkeFunnet.class);
		expectedException.expectMessage("Journalpost ikke funnet. journalpostId=123");
		expectedException.expect(hasProperty("faultInfo", hasProperty("feilaarsak", containsString(JournalpostIkkeFunnetException.class.getName()))));


		journalV3Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndPersistJournalpost("Dokumenttittel");
		HentDokumentRequest request = createRequest(journalpost);
		request.setDokumentId("123");

		setupExpectedException(NoDokumentInfoFoundException.class.getName());

		journalV3Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenFilDetaljerNotFoundWithGivenVariant() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndPersistJournalpost("Dokumenttittel");
		HentDokumentRequest request = createRequest(journalpost);
		Variantformater variantFormat = new Variantformater();
		variantFormat.setValue(VariantFormatCode.PRODUKSJON.name());
		request.setVariantformat(variantFormat);

		setupExpectedException(InvalidArgumentException.class.getName());

		journalV3Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenDokumentFilNotFound() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndPersistJournalpost("Dokumenttittel");
		HentDokumentRequest request = createRequest(journalpost);
		try {
			journalV3Provider.hentDokument(request);
		} catch (HentDokumentDokumentIkkeFunnet e) {
			assertThat(e.getFaultInfo(), isA(DokumentIkkeFunnet.class));
			assertThat(e.getFaultInfo().getFeilaarsak(), containsString("Could not find DokumentFil with filUuid=" + FIL_UUID));
		}
	}

	@Test
	public void shouldReturnDeletedDocument() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndPersistJournalpost(DELETED_DOCUMENT_TITLE);
		HentDokumentRequest request = createRequest(journalpost);

		persistDokumentFil();

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(FIL_CONTENT));
	}

	@Test
	public void shouldGetDocumentWhenMottakskanalAltinn() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndPersistJournalpost(MottaksKanalCode.ALTINN);
		HentDokumentRequest request = createRequest(journalpost);

		persistDokumentFil();

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(FIL_CONTENT));
	}

	@Test
	public void shouldGetDokument() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndPersistJournalpost("Dokumenttittel");
		HentDokumentRequest request = createRequest(journalpost);
		persistDokumentFil();

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(FIL_CONTENT));
	}

	@Test
	public void shouldGetSladdetDokument() throws Exception {
		abacPermit();

		Journalpost journalpost = buildAndPersistJournalpost("Dokumenttittel");
		HentDokumentRequest request = createRequest(journalpost);
		persistDokumentFil();
		persistDokumentFilSladdet();
		Begrensning begrensning = Begrensning.builder().begrensningId(1L)
				.begrensningType(SkjermingTypeCode.POL)
				.dokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.variantFormat(VariantFormatCode.ARKIV)
				.build();
		begrensning.setOpprettetKildeNavn("Clark Kentolini");
		begrensningRepository.save(begrensning);

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(FIL_CONTENT_SLADDET));
	}


	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldGetOnDemandDokument() throws Exception {
		resetAllRequests();
		abacPermit();
		Journalpost journalpost = joarkRepository.save(createOnDemandJournalpostBuilder().build());
		HentDokumentRequest request = createRequest(journalpost);
		stubFor(get(urlMatching("\\/joarkhentdokument\\?docToken=[a-zA-Z0-9\\-]+&mimetype=application%252Fpdf"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
						.withBody(ONDEMAND_FIL_CONTENT)));

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(ONDEMAND_FIL_CONTENT));
		DokumentUrlInfo dokumentUrlInfo = dokumentUrlInfoRepository.findByFilUuid(FIL_UUID);
		assertThat(dokumentUrlInfo.getDoctoken(), notNullValue());
	}

	private HentDokumentRequest createRequest(Journalpost journalpost) {
		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId(journalpost.getJournalpostId().toString());
		request.setDokumentId(journalpost.findAllDokumentInfos().iterator().next().getId().toString());
		Variantformater variantFormat = new Variantformater();
		variantFormat.setValue(VARIANT_FORMAT.name());
		request.setVariantformat(variantFormat);
		return request;
	}

	private void setupExpectedException(String rootCause) {
		expectedException.expect(HentDokumentDokumentIkkeFunnet.class);
		expectedException.expectMessage("Could not find document");
		expectedException.expect(hasProperty("faultInfo", hasProperty("feilaarsak", containsString(rootCause))));
	}

	private Journalpost buildAndPersistJournalpost(String dokumentTittel) {
		return joarkRepository.save(createJournalpostBuilder(dokumentTittel)
				.fagomrade(FagomradeCode.FOR).build());
	}

	private Journalpost buildAndPersistJournalpost(MottaksKanalCode mottaksKanalCode) {
		return joarkRepository.save(createJournalpostBuilder("tittel")
				.mottakskanal(mottaksKanalCode)
				.build());
	}

	private JournalpostBuilder createJournalpostBuilder(String dokumentTittel) {
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
																.filUuid(FIL_UUID_SLADDET).variantFormat(VARIANT_FORMAT_SLADDET)
																.opprettetKildeNavn("test").build()
												).build()).build());
	}

	private JournalpostBuilder createOnDemandJournalpostBuilder() {
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
												.tittel("Dokumentittel")
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.filUuid(FIL_UUID).variantFormat(VARIANT_FORMAT)
																.onDemandId(ON_DEMAND_ID).onDemandInstans(ON_DEMAND_INSTANS)
																.opprettetKildeNavn("test").build()).build()).build());
	}

	private void persistDokumentFilSladdet() {
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder().filUuid(FIL_UUID_SLADDET).fil(FIL_CONTENT_SLADDET).opprettetKildeNavn("test").build());
	}

	private void persistDokumentFil() {
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder().filUuid(FIL_UUID).fil(FIL_CONTENT).opprettetKildeNavn("test").build());
	}

}
