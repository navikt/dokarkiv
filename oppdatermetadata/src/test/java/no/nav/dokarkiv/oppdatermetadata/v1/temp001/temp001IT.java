package no.nav.dokarkiv.oppdatermetadata.v1.temp001;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataRequest;
import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataResponse;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.oppdatermetadata.v1.AbstractJournalfoerInngaaendeV1Itest;
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
public class temp001IT extends AbstractJournalfoerInngaaendeV1Itest {

	/**
	 * HVIS forsoekEndeligJF == TRUE, og ingen felter mangler for å endelig journalføre => returner 200 OK og journalpostId.
	 */
	@Test
	public void shouldFerdigstillJournalpostVedOppdateringUserTokenAndServiceUserToken() throws IOException {
		abacPermit();

		PutOppdatermetadataRequest request = mapper.readValue(classpathToString("__files/oppdatermetadata/happy_input_request.json"), PutOppdatermetadataRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		HttpEntity<PutOppdatermetadataRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<PutOppdatermetadataResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutOppdatermetadataResponse.class);

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
		assertThat(oppdatertJP.getAvsenderMottakerId(), is(request.getAvsenderMottaker().getIdentifikator()));
		assertThat(oppdatertJP.getAvsenderMottaker(), is(request.getAvsenderMottaker().getAvsenderMottakerNavn()));
		assertThat(oppdatertJP.getBrukere().size(), is(1));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId(), is(request.getBruker().getIdentifikator()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType(), is(BrukerTypeCode.PERSON));
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getSaksrelasjon().getSakId(), is(request.getArkivsak().getArkivsaksnummer()));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name(), is("FS22"));

		TestTransaction.end();
	}

	@Test
	public void shouldFerdigstillJournalpostVedOppdateringOnlyServiceUserToken() throws IOException {
		abacPermit();

		PutOppdatermetadataRequest request = mapper.readValue(classpathToString("__files/oppdatermetadata/happy_input_request.json"), PutOppdatermetadataRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);

		HttpEntity<PutOppdatermetadataRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<PutOppdatermetadataResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutOppdatermetadataResponse.class);

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
		assertThat(oppdatertJP.getAvsenderMottakerId(), is(request.getAvsenderMottaker().getIdentifikator()));
		assertThat(oppdatertJP.getAvsenderMottaker(), is(request.getAvsenderMottaker().getAvsenderMottakerNavn()));
		assertThat(oppdatertJP.getBrukere().size(), is(1));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId(), is(request.getBruker().getIdentifikator()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType(), is(BrukerTypeCode.PERSON));
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getSaksrelasjon().getSakId(), is(request.getArkivsak().getArkivsaksnummer()));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name(), is("FS22"));
		TestTransaction.end();
	}

	@Test
	public void shouldFailOnlyPersonUserToken() throws IOException {
		PutOppdatermetadataRequest request = mapper.readValue(classpathToString("__files/oppdatermetadata/happy_input_request.json"), PutOppdatermetadataRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);

		HttpEntity<PutOppdatermetadataRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<PutOppdatermetadataResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutOppdatermetadataResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpostPutJournalpost() throws IOException {
		abacDeny();

		PutOppdatermetadataRequest request = mapper.readValue(classpathToString("__files/oppdatermetadata/happy_input_request.json"), PutOppdatermetadataRequest.class);

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		HttpEntity<PutOppdatermetadataRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}
}


