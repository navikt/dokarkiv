package no.nav.dokarkiv.journalfoerinngaaende.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createVedleggDokumentInfo;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.BREVKODE_UPDATE;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMENT_TITTEL_UPDATE;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMNETTYPE_ID_UPDATE;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.response.Status;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.datautil.SkannetInnholdTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class JournalfoerInngaaendeV1IT extends AbstractJournalfoerInngaaendeV1Itest {

	private static final String JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER = "/rest/abac/journalfoer-inngaaende/v1/journalposter/";
	private ObjectMapper mapper = new ObjectMapper();

	/******************************
	 ** GetInngaaendeJournalpost **
	 ******************************/

	@Test
	public void shouldGetInngaaendeJournalpostByJournalpostId() throws Exception {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<GetJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.GET, createHeaders(), GetJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/getInngaaendejournalpost.json"), getOidcTokenBody(OIDC_TOKEN_SERVICE_USER_TEST
				.replace("Bearer ", ""))))));
		assertThat(responseEntity.getBody().getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.ENDELIG));
	}

	/**
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal feilmelding logges OG behandling avsluttes
	 **/
	@Test
	public void shouldReturnBadRequestInvalidInput() {
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + "NOT_A_NUMBER", HttpMethod.GET, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("journalpostId er ikke et tall. journalpostId=NOT_A_NUMBER"));
	}

	/**
	 * HVIS journalpostId ikke finnes i databasen, SÅ skal feilmelding gis (3) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnJournalpostNotFound() {
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + "123456", HttpMethod.GET, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Journalpost ikke funnet. journalpostId=123456"));
	}

	/**
	 * HVIS journalpostType != Inngående, SÅ skal feilmelding gis (4) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnBadRequestJournalpostErIkkeAvTypenInngaaende() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J)
				.journalpostType(JournalpostTypeCode.U));
		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.GET, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("er ikke av type Inngaaende"));
	}

	/**
	 * HVIS oppslag på journalpost returnerer Bruker har ikke tilgang til journalpost, SÅ skal dette logges til sporbarhetslogg og behandling avsluttes
	 **/
	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpost() {
		abacDeny();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.GET, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}

	/**
	 * HVIS tjenesten kalles med feil format på Oidc-token, SÅ skal feilmelding logges OG behandling avsluttes
	 **/
	@Test
	public void shouldReturnUnauthorizedUgyldigOidcToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, "Invalid-form.Oidc-token");

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.GET, new HttpEntity(headers), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Access Denied"));
	}

	/**
	 * HVIS journalpostId tilsvarer en journalpost med flere JournalpostDokumentInfoRelasjoner SÅ skal disse objektene returneres sortert,
	 * med Hoveddokument først, deretter Vedlegg sortert på opprettetDato med tidligst opprettet dato først
	 **/
	@Test
	public void shouldReturnSortedDocumentList() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn("itest")
				.tilknyttetAvNavn("itest")
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createVedleggDokumentInfo().tittel("siste dokument inn").build())
				.build());

		joarkRepository.save(journalpost);
		TestTransaction.start();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<GetJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.GET, createHeaders(), GetJournalpostResponse.class);

		assertThat(responseEntity.getBody().getDokumentListe().get(0).getTittel(), is("Gi meg foreldrepenger")); //Hoveddok
		assertThat(responseEntity.getBody().getDokumentListe().get(1).getTittel(), is("Takk skal du ha")); //Vedlegg1
		assertThat(responseEntity.getBody().getDokumentListe().get(2).getTittel(), is("siste dokument inn")); //Vedlegg2
	}

	/******************************
	 ** PutInngaaendeJournalpost **
	 ******************************/

	/**
	 * HVIS forsoekEndeligJF == TRUE, og ingen felter mangler for å endelig journalføre => returner 200 OK og journalpostId.
	 */
	@Test
	public void shouldFerdigstillJournalpostVedOppdatering() throws IOException {
		abacPermit();

		PutJournalpostRequest request = mapper.readValue(classpathToString("__files/put_journalpost/happy_input_request.json"), PutJournalpostRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		HttpEntity<PutJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<PutJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));
		assertThat(responseEntity.getBody().getMangler(), is(nullValue()));
		assertThat(responseEntity.getBody().getHarEndeligJF(), is(true));

		TestTransaction.start();
		Journalpost oppdatertJP = joarkRepository.findById(journalpostId).get();

		assertThat(oppdatertJP.getEndretAvNavn(), is(PERSON_USER_ID));
		assertThat(oppdatertJP.getEndretKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getChangeStamp().getUpdatedBy(), is(PERSON_USER_ID));
		assertThat(oppdatertJP.getInnhold(), is(request.getTittel()));
		assertThat(oppdatertJP.getFagomrade().name(), is(request.getTema()));
		assertThat(oppdatertJP.getJournalForendeEnhetId(), is(request.getJournalfEnhet()));
		assertThat(oppdatertJP.getAvsenderMottakerId(), is(request.getAvsender().getIdentifikator()));
		assertThat(oppdatertJP.getAvsenderMottaker(), is(request.getAvsender().getNavn()));
		assertThat(oppdatertJP.getBrukere().size(), is(1));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId(), is(request.getBruker().getIdentifikator()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType().name(), is(request.getBruker()
				.getBrukerType().value()));
		assertThat(oppdatertJP.getSaksrelasjon().getSakId(), is(request.getArkivSak().getArkivSakId()));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name(), is("FS22"));

		TestTransaction.end();
	}

	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpostPutJournalpost() throws IOException {
		abacDeny();

		PutJournalpostRequest request = mapper.readValue(classpathToString("__files/put_journalpost/happy_input_request.json"), PutJournalpostRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		HttpEntity<PutJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}

	/**
	 * HVIS forsoekEndeligJF == TRUE, og 1 eller flere felter mangler for endelig journalføring, returner 200 OK med Mangler-objekt
	 */
	@Test
	public void shouldReturnManglerVedForsoektEndeligJF() throws IOException {
		abacPermit();

		PutJournalpostRequest request = mapper.readValue(classpathToString("__files/put_journalpost/request_med_mangler.json"), PutJournalpostRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen")
				.innhold(null));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpEntity<PutJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<PutJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(journalpostId));
		assertThat(responseEntity.getBody().getMangler(), is(notNullValue()));
		assertThat(responseEntity.getBody().getMangler().getTittel(), is(Status.MANGLER));
		assertThat(responseEntity.getBody().getHarEndeligJF(), is(false));
	}

	/**
	 * HVIS journalpostType != Inngående, SÅ skal feilmelding gis (4) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnBadRequestJournalpostErIkkeAvTypenInngaaendePutJournalpost() throws IOException {
		abacPermit();

		PutJournalpostRequest request = mapper.readValue(classpathToString("__files/put_journalpost/request_med_mangler.json"), PutJournalpostRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen")
				.innhold(null));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpEntity<PutJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("er ikke av type Inngaaende"));
	}

	/**
	 * HVIS forsoekEndeligJF == FALSE, returner 200 OK
	 */
	@Test
	public void shouldReturnOKJournalpostIdVedOppdateringUtenEndeligJF() throws IOException {
		abacPermit();

		PutJournalpostRequest request = mapper.readValue(classpathToString("__files/put_journalpost/request_ikke_endeligJF.json"), PutJournalpostRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpEntity<PutJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<PutJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(journalpostId));
		assertThat(responseEntity.getBody().getMangler(), is(nullValue()));
		assertThat(responseEntity.getBody().getHarEndeligJF(), is(false));
	}

	/**
	 * HVIS journalpostId tilhører en utgående journalpost SÅ skal det kastes en feil og gi 400 BadRequest
	 */
	@Test
	public void shouldThrowExceptionHvisRequestvalideringFeiler() throws IOException {
		abacPermit();
		PutJournalpostRequest request = mapper.readValue(classpathToString("__files/put_journalpost/happy_input_request.json"), PutJournalpostRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpEntity<PutJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("ikke av type Inngaaende"));
	}

	/******************************
	 ** UpdateInngaaendeJournalpostDokument **
	 ******************************/

	@Test
	public void shouldUpdateDocument() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpHeaders headers = new HttpHeaders();
		HttpEntity httpEntity = new HttpEntity(new PutDokumentRequest().withDokumentTypeId(DOKUMNETTYPE_ID_UPDATE)
				.withNavSkjemaId(BREVKODE_UPDATE)
				.withTittel(DOKUMENT_TITTEL_UPDATE)
				.withDokumentKategori("SED"), oidcHeaders());

		Long dokumentId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		ResponseEntity<PutDokumentResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId, HttpMethod.PUT, httpEntity, PutDokumentResponse.class);

		assertThat(responseEntity.getBody().getDokumentId(), is(dokumentId.toString()));

		TestTransaction.start();
		DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokumentId).get();
		assertThat(dokumentInfo.getDokumenttypeId(), is(DOKUMNETTYPE_ID_UPDATE));
		assertThat(dokumentInfo.getBrevkode(), is(BREVKODE_UPDATE));
		assertThat(dokumentInfo.getTittel(), is(DOKUMENT_TITTEL_UPDATE));
		assertThat(dokumentInfo.getKategori().name(), is("SED"));
		assertThat(dokumentInfo.getEndretAvNavn(), is(PERSON_USER_ID));
		assertThat(dokumentInfo.getEndretKildeNavn(), is(SERVICE_USER_ID));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpostUpdateInngaaendeJournalpostDokument() {
		abacDeny();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);
		HttpEntity httpEntity = new HttpEntity(new PutDokumentRequest().withDokumentTypeId(DOKUMNETTYPE_ID_UPDATE)
				.withNavSkjemaId(BREVKODE_UPDATE)
				.withTittel(DOKUMENT_TITTEL_UPDATE)
				.withDokumentKategori("SED"), headers);

		Long dokumentId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId, HttpMethod.PUT, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}

	/**
	 * HVIS journalpostType != Inngående, SÅ skal feilmelding gis (4) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnBadRequestJournalpostErIkkeAvTypenInngaaendePutDocument() throws IOException {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);
		HttpEntity httpEntity = new HttpEntity(new PutDokumentRequest().withDokumentTypeId(DOKUMNETTYPE_ID_UPDATE)
				.withNavSkjemaId(BREVKODE_UPDATE)
				.withTittel(DOKUMENT_TITTEL_UPDATE)
				.withDokumentKategori("SED"), headers);

		Long dokumentId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId, HttpMethod.PUT, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("er ikke av type Inngaaende"));
	}

	@Test
	public void shouldNotUpdateNotFilledValues() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);
		HttpEntity httpEntity = new HttpEntity(new PutDokumentRequest()
				.withNavSkjemaId(BREVKODE_UPDATE)
				.withTittel(DOKUMENT_TITTEL_UPDATE), headers);

		Long dokumentId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo()
				.getDokumentInfoId();

		ResponseEntity<PutDokumentResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId, HttpMethod.PUT, httpEntity, PutDokumentResponse.class);


		assertThat(responseEntity.getBody().getDokumentId(), is(dokumentId.toString()));
		DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokumentId).get();
		assertThat(dokumentInfo.getDokumenttypeId(), is("I0001"));
		assertThat(dokumentInfo.getBrevkode(), is(BREVKODE_UPDATE));
		assertThat(dokumentInfo.getTittel(), is(DOKUMENT_TITTEL_UPDATE));
		assertThat(dokumentInfo.getKategori().name(), is("SOK"));
	}

	@Test
	public void shouldFailWhenDokumentInfoNotFound() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);
		HttpEntity httpEntity = new HttpEntity(new PutDokumentRequest()
				.withNavSkjemaId(BREVKODE_UPDATE)
				.withTittel(DOKUMENT_TITTEL_UPDATE), headers);

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + "***gammelt_fnr***58468464" + "/dokumenter/" + "***gammelt_fnr******gammelt_fnr***48", HttpMethod.PUT, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	@Test
	public void shouldFailWhenInvalidDokumentKategori() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);
		HttpEntity httpEntity = new HttpEntity(new PutDokumentRequest().withDokumentKategori("SJO"), headers);

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId, HttpMethod.PUT, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("SJO er ugyldig verdi for dokumentKategori"));
	}

	/***********************
	 ** PostLogiskVedlegg **
	 ***********************/

	@Test
	public void shouldPostLogiskVedlegg() throws Exception {
		abacPermit();
		PostLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/post_logisk_vedlegg_happy_input_request.json"), PostLogiskVedleggRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();

		HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<PostLogiskVedleggResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, PostLogiskVedleggResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody(), is(notNullValue()));

		TestTransaction.start();
		SkannetInnhold skannetinnhold = skannetInnholdRepository.findById(Long.parseLong(responseEntity.getBody()
				.getLogiskVedleggId())).get();

		assertThat(skannetinnhold, is(notNullValue()));
		assertThat(skannetinnhold.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertThat(skannetinnhold.getChangeStamp().getCreatedBy(), is(PERSON_USER_ID));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpostPostLogiskVedlegg() throws IOException {
		abacDeny();
		PostLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/post_logisk_vedlegg_happy_input_request.json"), PostLogiskVedleggRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();

		HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}

	/**
	 * HVIS journalpostType != Inngående, SÅ skal feilmelding gis (4) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnBadRequestJournalpostErIkkeAvTypenInngaaendePostLogiskVedlegg() throws IOException {
		abacPermit();
		PostLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/post_logisk_vedlegg_happy_input_request.json"), PostLogiskVedleggRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();

		HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("er ikke av type Inngaaende"));
	}

	@Test
	public void shouldReturnJournalpostIdNotFoundExceptionPostVedlegg() {
		abacPermit();

		HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(new PostLogiskVedleggRequest(), oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + 123443546 + "/dokumenter/" + 1234 + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Journalpost ikke funnet"));
	}

	@Test
	public void shouldReturnDokumentinfoIdNotFoundExceptionPostVedlegg() {
		abacPermit();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();

		HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(new PostLogiskVedleggRequest(), oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + 1234 + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Fant ingen dokument med dokumentId=1234"));
	}

	/***************************
	 ** OppdaterLogiskVedlegg **
	 ***************************/

	@Test
	public void shouldUpdateLogiskVedlegg() throws Exception {
		abacPermit();
		PutLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/put_logisk_vedlegg_happy_input_request.json"), PutLogiskVedleggRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();
		String logiskVedleggId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.getSkannetInnholdId()
				.toString();

		HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody(), containsString("Oppdatering av logiskVedlegg med logiskVedleggId="));

		TestTransaction.start();
		Journalpost resultJournalpost = joarkRepository.findById(Long.parseLong(journalpostId)).get();
		SkannetInnhold skannetInnhold = resultJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
				.getSkannetInnholdListe().iterator().next();

		assertThat(skannetInnhold.getVedleggInnhold(), is("Dette er en tittel"));
		assertThat(skannetInnhold.getEndretKildeNavn(), is(SERVICE_USER_ID));
		assertThat(skannetInnhold.getChangeStamp().getUpdatedBy(), is(PERSON_USER_ID));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpostPutLogiskVedlegg() throws IOException {
		abacDeny();
		PutLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/put_logisk_vedlegg_happy_input_request.json"), PutLogiskVedleggRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();
		String logiskVedleggId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.getSkannetInnholdId()
				.toString();

		HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}

	/**
	 * HVIS journalpostType != Inngående, SÅ skal feilmelding gis (4) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnBadRequestJournalpostErIkkeAvTypenInngaaendePutLogiskVedlegg() throws IOException {
		abacPermit();
		PutLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/put_logisk_vedlegg_happy_input_request.json"), PutLogiskVedleggRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();
		String logiskVedleggId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.getSkannetInnholdId()
				.toString();

		HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("er ikke av type Inngaaende"));
	}

	@Test
	public void shouldReturnJournalpostIdNotFoundExceptionPutVedlegg() {
		abacPermit();

		HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(new PutLogiskVedleggRequest(), oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + "123443546" + "/dokumenter/" + "1234" + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Journalpost ikke funnet"));
	}

	@Test
	public void shouldReturnDokumentinfoIdNotFoundExceptionPutVedlegg() {
		abacPermit();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();

		HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(new PutLogiskVedleggRequest(), oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + 1234 + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Fant ingen dokument med dokumentId=1234"));
	}

	@Test
	public void shouldReturnLogiskVedleggIdNotFoundExceptionPutLogiskVedlegg() {
		abacPermit();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();
		String logiskVedleggId = "***gammelt_fnr***7965";

		HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(new PutLogiskVedleggRequest(), oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Kunne ikke finne logisk vedlegg"));
	}

	/*************************
	 ** DeleteLogiskVedlegg **
	 *************************/

	@Test
	public void shouldDeleteLogiskVedlegg() {
		abacPermit();

		//Create and save testdata
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();
		String logiskVedleggId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.getSkannetInnholdId()
				.toString();

		journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
				.addSkannetInnhold(SkannetInnholdTestDataProvider.createSkannetInnhold().build());

		TestTransaction.start();
		joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		//Start test
		assertThat(journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
				.getSkannetInnholdListe().size(), is(2));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.DELETE, createHeaders(), String.class);

		TestTransaction.start();
		Journalpost resultJournalpost = joarkRepository.findById(Long.parseLong(journalpostId)).get();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(resultJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
				.getSkannetInnholdListe().size(), is(1));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpostDeleteLogiskVedlegg() {
		abacDeny();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();
		String logiskVedleggId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.getSkannetInnholdId()
				.toString();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.DELETE, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}

	/**
	 * HVIS journalpostType != Inngående, SÅ skal feilmelding gis (4) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnBadRequestJournalpostErIkkeAvTypenInngaaendeDeleteLogiskVedlegg() throws IOException {
		abacPermit();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();
		String logiskVedleggId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.getSkannetInnholdId()
				.toString();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.DELETE, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("er ikke av type Inngaaende"));
	}


	@Test
	public void shouldReturnJournalpostNotFoundExceptionDeleteLogiskVedlegg() {
		abacPermit();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + 123435545 + "/dokumenter/" + 546546546 + "/logiskeVedlegg/" + 546546546, HttpMethod.DELETE, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Journalpost ikke funnet"));
	}

	@Test
	public void shouldReturnLogiskVedleggIdNotFoundException() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId()
				.toString();
		String logiskVedleggId = "***gammelt_fnr***7965";

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.DELETE, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Kunne ikke finne logisk vedlegg"));
	}

	@Test
	public void shouldReturnDokumentinfoIdNotFoundException() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		String journalpostId = journalpost.getJournalpostId().toString();
		String dokumentId = "1234546636";
		String logiskVedleggId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.getSkannetInnholdId()
				.toString();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.DELETE, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Fant ingen dokument med dokumentId=1234546636"));
	}

}

