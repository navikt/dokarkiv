package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark003i;

import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.BREVKODE_UPDATE;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMENT_TITTEL_UPDATE;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMNETTYPE_ID_UPDATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
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
public class Rjoark003iIT extends AbstractJournalfoerInngaaendeV1Itest {

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

}