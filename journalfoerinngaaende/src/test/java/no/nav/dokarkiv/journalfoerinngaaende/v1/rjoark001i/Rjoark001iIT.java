package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark001i;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createVedleggDokumentInfo;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
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

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Rjoark001iIT extends AbstractJournalfoerInngaaendeV1Itest {

	@Test
	public void shouldGetInngaaendeJournalpostByJournalpostIdUserTokenAndServiceUserToken() throws Exception {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<GetJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.GET, createHeaders(), GetJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/getInngaaendejournalpost_PersonUser_and_ServiceUser.json"),
				getOidcTokenBody(OIDC_TOKEN_PERSON_USER_TEST.replace("Bearer ", "")),
				getOidcTokenBody(OIDC_TOKEN_SERVICE_USER_TEST.replace("Bearer ", ""))))));
		assertThat(responseEntity.getBody().getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.ENDELIG));
	}

	@Test
	public void shouldGetInngaaendeJournalpostByJournalpostIdOnlyServiceUserToken() throws Exception {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);

		ResponseEntity<GetJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.GET, new HttpEntity(headers), GetJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/getInngaaendejournalpost_only_ServiceUser.json"),
				getOidcTokenBody(OIDC_TOKEN_SERVICE_USER_TEST.replace("Bearer ", ""))))));
		assertThat(responseEntity.getBody().getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.ENDELIG));
	}

	@Test
	public void shoulFailOnlyUserToken() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);

		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.GET, new HttpEntity(headers),
				RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
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

}

