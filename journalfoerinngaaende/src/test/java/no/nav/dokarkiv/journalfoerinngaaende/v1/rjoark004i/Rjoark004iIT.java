package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark004i;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.datautil.SkannetInnholdTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.journalfoerinngaaende.v1.AbstractJournalfoerInngaaendeV1Itest;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Rjoark004iIT extends AbstractJournalfoerInngaaendeV1Itest {

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

