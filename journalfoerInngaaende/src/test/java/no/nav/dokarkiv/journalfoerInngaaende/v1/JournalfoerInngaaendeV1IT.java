package no.nav.dokarkiv.journalfoerInngaaende.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createVedleggDokumentInfo;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.JournalpostResponseTo;
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
public class JournalfoerInngaaendeV1IT extends AbstractJournalfoerInngaaendeV1Itest {

	@Test
	public void shouldGetInngaaendeJournalpostByJournalpostId() throws Exception {
		abacPermit();

		joarkRepository.deleteAll();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<JournalpostResponseTo> responseEntity = restTemplate.exchange(
				"/rest/journalfoer-inngaaende/v1/journalposter/" + journalpostId, HttpMethod.GET, createHeaders(), JournalpostResponseTo.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/getInngaaendejournalpost.json"))));
		assertThat(responseEntity.getBody().getJournaltilstand(), is("ENDELIG"));
	}

	/**
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal feilmelding logges OG behandling avsluttes
	 **/

	@Test
	public void shouldReturnBadRequestInvalidInput() {
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				"/rest/journalfoer-inngaaende/v1/journalposter/" + "NOT_A_NUMBER", HttpMethod.GET, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), is("journalpostId er ikke et tall. journalpostId=NOT_A_NUMBER"));
	}

	/**
	 * HVIS journalpostId ikke finnes i databasen, SÅ skal feilmelding gis (3) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnJournalpostNotFound() {
		joarkRepository.deleteAll();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				"/rest/journalfoer-inngaaende/v1/journalposter/" + "123456", HttpMethod.GET, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), is("Kunne ikke finne journalpost i Joark. journalpostId=123456"));
	}

	/**
	 * HVIS journalpostType != Inngående, SÅ skal feilmelding gis (4) og behandling avsluttes
	 **/
	@Test
	public void shouldReturnBadRequestJournalpostErIkkeAvTypenInngaaende() {
		abacPermit();
		joarkRepository.deleteAll();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J).journalpostType(JournalpostTypeCode.U));
		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				"/rest/journalfoer-inngaaende/v1/journalposter/" + journalpostId, HttpMethod.GET, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), is("Journalpost er ikke av type Inngaaende. journalpostId=" + journalpostId));
	}

	/**
	 * HVIS oppslag på journalpost returnerer Bruker har ikke tilgang til journalpost, SÅ skal dette logges til sporbarhetslogg og behandling avsluttes
	 **/
	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpost() {
		abacDeny();
		joarkRepository.deleteAll();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		String journalpostId = journalpost.getJournalpostId().toString();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				"/rest/journalfoer-inngaaende/v1/journalposter/" + journalpostId, HttpMethod.GET, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), is("Bruker har ikke tilgang til journalpost. journalpostId=" + journalpostId));
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
				"/rest/journalfoer-inngaaende/v1/journalposter/" + journalpostId, HttpMethod.GET, new HttpEntity(headers), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
		assertThat(responseEntity.getBody(), containsString("Kunne ikke autorisere forespoersel."));
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

		ResponseEntity<JournalpostResponseTo> responseEntity = restTemplate.exchange(
				"/rest/journalfoer-inngaaende/v1/journalposter/" + journalpostId, HttpMethod.GET, createHeaders(), JournalpostResponseTo.class);

		assertThat(responseEntity.getBody().getDokumenter().get(0).getTittel(), is("Gi meg foreldrepenger")); //Hoveddok
		assertThat(responseEntity.getBody().getDokumenter().get(1).getTittel(), is("Takk skal du ha")); //Vedlegg1
		assertThat(responseEntity.getBody().getDokumenter().get(2).getTittel(), is("siste dokument inn")); //Vedlegg2
	}
}

