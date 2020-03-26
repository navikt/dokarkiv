package no.nav.dokarkiv.journalpost.v1.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.AKTOER_ID;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.FAIL_AKTOER_ID;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.FNR;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.identInspectionObjects;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.INNHOLD;
import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22;
import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.PEN;
import static no.nav.dokarkiv.core.security.JwtClaimsBuilderProvider.openAmClaimsBuilder;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SYM;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_TIL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createFagsak;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createGenerellSak;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OppdaterJournalpostIT extends AbstractJournalpostIT {

	private static final String IDENTIFIKATOR = "***gammelt_fnr***";
	private static final String AVSENDER_MOTTAKER_NAVN = "etternavn, fornavn";
	private static final AvsenderMottakerIdType AVSENDER_MOTTAKER_TYPE_ID = AvsenderMottakerIdType.FNR;
	private static final String ARKIVSAKSNUMMER = "123123";
	private static final String TEMA = "FOR";
	private static final String BEHANDLINGSTEMA = "ab0001";
	private static final String TITTEL = "Ettersendelse av something";
	private static final String AVSENDER_MOTTAKER_LAND = "Legoland";
	private static final String NOKKEL = "nokkel";
	private static final String VERDI = "verdi";
	private static final String BREVKODE = "brevkode";
	private static final String JOURNALFOERENDE_ENHET = "9999";
	private static final String SERVICE_USER_ID = "srvdokarkiv";

	@Before
	public void setUp() {
		OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
				.build());
		OIDC_TOKEN_SERVICE_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build());
	}

	/**
	 * HVIS forsoekEndeligJF == TRUE, og ingen felter mangler for å endelig journalføre => returner 200 OK og journalpostId.
	 */
	@Test
	public void shouldFerdigstillJournalpostVedOppdateringUserTokenAndServiceUserToken() throws IOException {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		OppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/putInngaaendejournalpost_PersonUser_and_ServiceUser.json"),
				getOidcTokenBody(OIDC_TOKEN_PERSON_USER_TEST.replace("Bearer ", "")),
				getOidcTokenBody(OIDC_TOKEN_SERVICE_USER_TEST.replace("Bearer ", ""))))));


		TestTransaction.start();
		Journalpost oppdatertJP = joarkRepository.findById(journalpostId).get();

		assertThat(oppdatertJP.getEndretAvNavn(), is(PERSON_USER_ID));
		assertThat(oppdatertJP.getEndretKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getChangeStamp().getUpdatedBy(), is(PERSON_USER_ID));
		assertThat(oppdatertJP.getInnhold(), is(request.getTittel()));
		assertThat(oppdatertJP.getFagomrade().name(), is(request.getTema()));
		assertThat(oppdatertJP.getBehandlingstema().name(), is(request.getBehandlingstema()));
		assertThat(oppdatertJP.getJournalForendeEnhetId(), is(request.getJournalfoerendeEnhet()));
		assertThat(oppdatertJP.getLand(), is(request.getAvsenderMottaker().getLand()));
		assertThat(oppdatertJP.getAvsenderMottakerId(), is(request.getAvsenderMottaker().getId()));
		assertThat(oppdatertJP.getAvsenderMottaker(), is(request.getAvsenderMottaker().getNavn()));
		assertThat(oppdatertJP.getAvsenderMottakerIdType(), is(AvsenderMottakerIdTypeCode.FNR));
		assertThat(oppdatertJP.getBrukere().size(), is(1));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId(), is(request.getBruker().getId()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType(), is(BrukerTypeCode.PERSON));
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getSaksrelasjon().getSakId(), is(request.getSak().getArkivsaksnummer()));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name(), is("FS22"));
		assertThat(oppdatertJP.getTilleggsopplysninger().size(), is(1));
		assert (oppdatertJP.getTilleggsopplysninger().containsKey(request.getTilleggsopplysninger().get(0).getNokkel()));
		assert (oppdatertJP.getTilleggsopplysninger().containsValue(request.getTilleggsopplysninger().get(0).getVerdi()));
		assertThat(oppdatertJP.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(request.getDokumenter()
						.get(0)
						.getDokumentInfoId())).getTittel(),
				is(request.getDokumenter().get(0).getTittel()));
		assertThat(oppdatertJP.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(request.getDokumenter()
						.get(0)
						.getDokumentInfoId())).getBrevkode(),
				is(request.getDokumenter().get(0).getBrevkode()));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());

		assertEquals(3, aksjonsLoggList.size());

		assertEquals(PERSON_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getApplikasjon());
		assertEquals(AksjonsTypeCode.ENDRE_METADATA, aksjonsLoggList.get(0).getAksjon());
		assertEquals(3, aksjonsLoggList.get(0).getArkivElementEndringer().size());

		assertEquals(PERSON_USER_ID, aksjonsLoggList.get(1).getUtfoertAv());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(1).getApplikasjon());
		assertEquals(AksjonsTypeCode.SAKSTILKNYTNING, aksjonsLoggList.get(1).getAksjon());
		assertEquals(2, aksjonsLoggList.get(1).getArkivElementEndringer().size());

		assertEquals(PERSON_USER_ID, aksjonsLoggList.get(2).getUtfoertAv());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(2).getApplikasjon());
		assertEquals(AksjonsTypeCode.ENDRE_METADATA, aksjonsLoggList.get(2).getAksjon());
		assertEquals(2, aksjonsLoggList.get(2).getArkivElementEndringer().size());

		TestTransaction.end();
	}

	@Test
	public void shouldNotProduceAksjonsLoggForEmptyRequest() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = new OppdaterJournalpostRequest();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.start();
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assert (aksjonsLoggList.isEmpty());
		TestTransaction.end();
	}

	@Test
	public void shouldUpdateJournalpostWithSaksrelasjonIsNull() {
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen")
				.saksrelasjon(null);
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.start();
		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId(), is(ARKIVSAKSNUMMER));
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(FS22));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList, hasSize(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AksjonsTypeCode.SAKSTILKNYTNING));
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer(), hasSize(2));
		TestTransaction.end();
	}

	@Test
	public void shouldNotProduceAksjonsLoggForUnchangedFields() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema("PEN")
				.tittel(INNHOLD)
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.start();
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assert (aksjonsLoggList.isEmpty());
		TestTransaction.end();
	}

	@Test
	public void shouldFerdigstillJournalpostVedOppdateringOnlyServiceUserToken() throws IOException {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		OppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/putInngaaendejournalpost_only_ServiceUser.json"),
				getOidcTokenBody(OIDC_TOKEN_SERVICE_USER_TEST.replace("Bearer ", ""))))));


		TestTransaction.start();
		Journalpost oppdatertJP = joarkRepository.findById(journalpostId).get();

		assertThat(oppdatertJP.getEndretAvNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getEndretKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getChangeStamp().getUpdatedBy(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getInnhold(), is(request.getTittel()));
		assertThat(oppdatertJP.getFagomrade().name(), is(request.getTema()));
		assertThat(oppdatertJP.getAvsenderMottakerId(), is(request.getAvsenderMottaker().getId()));
		assertThat(oppdatertJP.getAvsenderMottaker(), is(request.getAvsenderMottaker().getNavn()));
		assertThat(oppdatertJP.getAvsenderMottakerIdType(), is(AvsenderMottakerIdTypeCode.FNR));
		assertThat(oppdatertJP.getBrukere().size(), is(1));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId(), is(request.getBruker().getId()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType(), is(BrukerTypeCode.PERSON));
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getSaksrelasjon().getSakId(), is(request.getSak().getArkivsaksnummer()));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name(), is("FS22"));
		TestTransaction.end();
	}

	@Test
	public void shouldFailOnlyPersonUserToken() {
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		OppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpostPutJournalpost() {
		abacDeny();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		OppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}

	@Test
	public void happyPathGsakArkivsak() {
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.sakstype(Sakstype.ARKIVSAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		TestTransaction.start();
		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId(), is(ARKIVSAKSNUMMER));
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(FS22));
		TestTransaction.end();
	}

	@Test
	public void happyPathGsakArkivsakSakstypeIkkeAngitt() {
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		TestTransaction.start();
		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId(), is(ARKIVSAKSNUMMER));
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(FS22));
		TestTransaction.end();
	}

	@Test
	public void happyPathPsakArkivsak() {
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.PSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.sakstype(Sakstype.ARKIVSAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		TestTransaction.start();
		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId(), is(ARKIVSAKSNUMMER));
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(PEN));
		TestTransaction.end();
	}

	@Test
	public void happyPathNyGenerellSak() {
		clearSakRepository();
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));
		assertEquals(sakRepository.count(), 1);

		TestTransaction.start();
		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();

		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA);
		assertTrue(isBlank(sak.getFagsakNr()));
		assertEquals(sak.getApplikasjon(), FS22.name());

		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertEquals(oppdatertJournalpost.getSaksrelasjon().getSakId(), sak.getSakId().toString());
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(FS22));
		TestTransaction.end();
	}

	@Test
	public void happyPathEksisterendeGenerellSak() {
		clearSakRepository();
		abacPermit();

		no.nav.dokarkiv.core.domain.entities.Sak sak = createGenerellSak();
		sakRepository.save(sak);
		commitAndStartNewTransaction();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");

		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		assertEquals(sakRepository.count(), 1);

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());
		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		TestTransaction.start();
		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId().toString());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
		TestTransaction.end();
	}

	@Test
	public void happyPathNyFagsak() {
		clearSakRepository();
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));
		assertEquals(sakRepository.count(), 1);

		TestTransaction.start();
		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA);
		assertEquals(sak.getFagsakNr(), FAGSAK_ID);
		assertEquals(sak.getApplikasjon(), Fagsaksystem.AO01.name());

		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertEquals(oppdatertJournalpost.getSaksrelasjon().getSakId(), sak.getSakId().toString());
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(FS22));
		TestTransaction.end();

	}

	@Test
	public void happyPathNyFagsakAktoerId() {
		clearSakRepository();
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(BrukerIdType.AKTOERID).id(AKTOER_ID).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertEquals(sakRepository.count(), 1);

		TestTransaction.start();
		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);

		no.nav.dokarkiv.core.domain.entities.Bruker bruker = joarkRepository.findAll()
				.iterator()
				.next()
				.getBrukere()
				.iterator()
				.next();
		assertEquals(bruker.getBrukerId(), FNR);
		assertEquals(bruker.getBrukerType(), BrukerTypeCode.PERSON);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(2, aksjonsLoggList.size());
		assertEquals(PERSON_USER_ID, aksjonsLoggList.get(1).getUtfoertAv());
		TestTransaction.end();

	}

	@Test
	public void shouldOppdattereJournalpostWithoutBrukerWhenFnrNotFound() {
		clearSakRepository();
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(BrukerIdType.AKTOERID).id(FAIL_AKTOER_ID).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertEquals(sakRepository.count(), 1);
		TestTransaction.start();
		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), FAIL_AKTOER_ID);

		assertEquals(joarkRepository.findAll().iterator().next().getBrukere().size(), 0);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(2, aksjonsLoggList.size());
		TestTransaction.end();
	}


	@Test
	public void happyPathNyFagsakOrgnr() {
		clearSakRepository();
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen")
				.brukere(BrukerTestDataProvider.createBruker("***gammelt_fnr***", BrukerTypeCode.PERSON));
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(BrukerIdType.ORGNR).id(BRUKER_ID_ORGANISASJON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));
		assertEquals(sakRepository.count(), 1);

		TestTransaction.start();
		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getOrgnr(), BRUKER_ID_ORGANISASJON);

		no.nav.dokarkiv.core.domain.entities.Bruker bruker = joarkRepository.findAll()
				.iterator()
				.next()
				.getBrukere()
				.iterator()
				.next();
		assertEquals(bruker.getBrukerId(), BRUKER_ID_ORGANISASJON);
		assertEquals(bruker.getBrukerType(), BrukerTypeCode.ORGANISASJON);

		TestTransaction.end();
	}

	@Test
	public void happyPathEksisterendeFagsak() {
		clearSakRepository();
		abacPermit();

		no.nav.dokarkiv.core.domain.entities.Sak sak = createFagsak();
		sakRepository.save(sak);
		commitAndStartNewTransaction();

		assertEquals(sakRepository.count(), 1);

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));
		assertEquals(sakRepository.count(), 1);

		TestTransaction.start();

		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertEquals(oppdatertJournalpost.getSaksrelasjon().getSakId(), sak.getSakId().toString());
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(FS22));

		TestTransaction.end();
	}


	@Test
	public void happyPathFagsakPesys() {
		clearSakRepository();
		abacPermit();

		long sakRepositoryCount = sakRepository.count();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.PP01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		TestTransaction.start();

		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(PEN));
		assertEquals(sakRepository.count(), sakRepositoryCount);

		TestTransaction.end();

	}

	@Test
	public void shouldUpdateWhenTemaPENAndGenerellSak() {
		clearSakRepository();
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(FS22));
	}

	@Test
	public void shouldUpdateWhenTemaUFOAndGenerellSak() {
		clearSakRepository();
		abacPermit();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));

		Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem(), is(FS22));
	}

	@Test
	public void shouldCallAktoerService() {
		clearSakRepository();
		abacPermit();

		int identInspectionObjectSize = identInspectionObjects.size();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.KONT)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());
		restTemplate.exchange(URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertEquals(identInspectionObjectSize + 1, identInspectionObjects.size());
	}

	@Test
	public void shouldNotCallAktoerServiceWithoutBrukerIdTypeFNR() {
		clearSakRepository();
		abacPermit();

		int identInspectionObjectSize = identInspectionObjects.size();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.AKTOERID).id(AKTOER_ID).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.KONT)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());
		restTemplate.exchange(URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertEquals(identInspectionObjectSize, identInspectionObjects.size());
	}

	@Test
	public void shouldNotCallAktoerServiceWithoutSakstype() {
		clearSakRepository();
		abacPermit();

		int identInspectionObjectSize = identInspectionObjects.size();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());
		restTemplate.exchange(URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertEquals(identInspectionObjectSize, identInspectionObjects.size());
	}

	@Test
	public void shouldNotCallAktoerServiceWithSAKFagsystemPP01() {
		clearSakRepository();
		abacPermit();

		int identInspectionObjectSize = identInspectionObjects.size();

		JournalpostBuilder journalpostBuilder = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen");
		Journalpost journalpost = buildAndCommit(journalpostBuilder);
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.PP01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());
		restTemplate.exchange(URL_JOURNALPOST + journalpostId, HttpMethod.PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertEquals(identInspectionObjectSize, identInspectionObjects.size());
	}

	private OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithDokumentInfoId(Long dokumentInfoId) {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(IDENTIFIKATOR)
						.idType(AVSENDER_MOTTAKER_TYPE_ID)
						.navn(AVSENDER_MOTTAKER_NAVN)
						.land(AVSENDER_MOTTAKER_LAND)
						.build())
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(IDENTIFIKATOR)
						.build())
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.build())
				.tema(TEMA)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(TITTEL)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.tilleggsopplysninger(Arrays.asList(Tilleggsopplysning.builder()
						.nokkel(NOKKEL)
						.verdi(VERDI)
						.build()))
				.dokumenter(Arrays.asList(DokumentInfo.builder()
						.dokumentInfoId(Long.toString(dokumentInfoId))
						.brevkode(BREVKODE)
						.tittel(TITTEL)
						.build()))
				.build();
	}
}


