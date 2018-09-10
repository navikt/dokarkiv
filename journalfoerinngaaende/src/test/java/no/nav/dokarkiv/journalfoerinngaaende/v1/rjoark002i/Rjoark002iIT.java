package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark002i;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.response.Status;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalfoerinngaaende.v1.AbstractJournalfoerInngaaendeV1Itest;
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
public class Rjoark002iIT extends AbstractJournalfoerInngaaendeV1Itest {

	/**
	 * HVIS forsoekEndeligJF == TRUE, og ingen felter mangler for å endelig journalføre => returner 200 OK og journalpostId.
	 */
	@Test
	public void shouldFerdigstillJournalpostVedOppdateringUserTokenAndServiceUserToken() throws IOException {
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
		assertThat(oppdatertJP.getJournalForendeEnhetId(), is(request.getJournalfEnhet()));
		assertThat(oppdatertJP.getAvsenderMottakerId(), is(request.getAvsender().getIdentifikator()));
		assertThat(oppdatertJP.getAvsenderMottaker(), is(request.getAvsender().getNavn()));
		assertThat(oppdatertJP.getBrukere().size(), is(1));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId(), is(request.getBruker().getIdentifikator()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType().name(), is(request.getBruker()
				.getBrukerType().value()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getSaksrelasjon().getSakId(), is(request.getArkivSak().getArkivSakId()));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name(), is("FS22"));

		TestTransaction.end();
	}

	@Test
	public void shouldFerdigstillJournalpostVedOppdateringOnlyServiceUserToken() throws IOException {
		abacPermit();

		PutJournalpostRequest request = mapper.readValue(classpathToString("__files/put_journalpost/happy_input_request.json"), PutJournalpostRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);

		HttpEntity<PutJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<PutJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().getJournalpostId(), is(String.valueOf(journalpostId)));
		assertThat(responseEntity.getBody().getMangler(), is(nullValue()));
		assertThat(responseEntity.getBody().getHarEndeligJF(), is(true));

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/putInngaaendejournalpost_only_ServiceUser.json"),
				getOidcTokenBody(OIDC_TOKEN_SERVICE_USER_TEST.replace("Bearer ", ""))))));


		TestTransaction.start();
		Journalpost oppdatertJP = joarkRepository.findById(journalpostId).get();

		assertThat(oppdatertJP.getEndretAvNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getEndretKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getChangeStamp().getUpdatedBy(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getInnhold(), is(request.getTittel()));
		assertThat(oppdatertJP.getFagomrade().name(), is(request.getTema()));
		assertThat(oppdatertJP.getJournalForendeEnhetId(), is(request.getJournalfEnhet()));
		assertThat(oppdatertJP.getAvsenderMottakerId(), is(request.getAvsender().getIdentifikator()));
		assertThat(oppdatertJP.getAvsenderMottaker(), is(request.getAvsender().getNavn()));
		assertThat(oppdatertJP.getBrukere().size(), is(1));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId(), is(request.getBruker().getIdentifikator()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType().name(), is(request.getBruker()
				.getBrukerType().value()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getSaksrelasjon().getSakId(), is(request.getArkivSak().getArkivSakId()));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name(), is("FS22"));
		TestTransaction.end();
	}

	@Test
	public void shouldFailOnlyPersonUserToken() throws IOException {
		PutJournalpostRequest request = mapper.readValue(classpathToString("__files/put_journalpost/happy_input_request.json"), PutJournalpostRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);

		HttpEntity<PutJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<PutJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
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

}


