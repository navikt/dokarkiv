package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark004i;

import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.datautil.SkannetInnholdTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.journalfoerinngaaende.v1.AbstractJournalfoerInngaaendeV1Itest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Rjoark004iIT extends AbstractJournalfoerInngaaendeV1Itest {

    /***************************
     ** PersistLogiskVedlegg **
     ***************************/

    @Test
    public void shouldPostLogiskVedleggUserTokenAndServiceUserToken() throws Exception {
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

        verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/postLogiskVedlegg_PersonUser_and_ServiceUser.json"),
                getBearerTokenBody(requestHttpEntity.getHeaders(), HttpHeaders.AUTHORIZATION),
                getBearerTokenBody(requestHttpEntity.getHeaders(), NAV_CONSUMER_TOKEN)))));

        TestTransaction.start();
        SkannetInnhold skannetinnhold = skannetInnholdRepository.findById(Long.parseLong(responseEntity.getBody()
                .getLogiskVedleggId())).get();

        assertThat(skannetinnhold, is(notNullValue()));
        assertThat(skannetinnhold.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
        assertThat(skannetinnhold.getChangeStamp().getCreatedBy(), is(PERSON_USER_ID));
        TestTransaction.end();
    }

    @Test
    public void shouldPostLogiskVedleggOnlyServiceUserToken() throws Exception {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + restStsToken(SERVICE_USER_ID));

        HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<PostLogiskVedleggResponse> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, PostLogiskVedleggResponse.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), is(notNullValue()));

        verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/postLogiskVedlegg_only_ServiceUser.json"),
                getBearerTokenBody(requestHttpEntity.getHeaders(), HttpHeaders.AUTHORIZATION)))));

        TestTransaction.start();
        SkannetInnhold skannetinnhold = skannetInnholdRepository.findById(Long.parseLong(responseEntity.getBody()
                .getLogiskVedleggId())).get();

        assertThat(skannetinnhold, is(notNullValue()));
        assertThat(skannetinnhold.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
        assertThat(skannetinnhold.getChangeStamp().getCreatedBy(), is(SERVICE_USER_ID));
        TestTransaction.end();
    }

    @Test
    public void shouldThrowExceptionWhenPostLogiskVedleggIsNull() throws Exception {
        abacPermit();
        String tittle = classpathToString("__files/logiskvedlegg/post_logisk_vedlegg_feil_input_request.json");
        PostLogiskVedleggRequest request = mapper.readValue(tittle == null ? null : tittle, PostLogiskVedleggRequest.class);

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

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), containsString("Tittelen kan ikke være null eller tom"));
    }

    @Test
    public void shouldFailToPostLogiskVedleggOnlyPersonUserToken() throws Exception {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, openAmToken(PERSON_USER_ID));

        HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, RestConsumerExceptionResponse.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
        assertThat(responseEntity.getBody(), is(notNullValue()));
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
     * HVIS journalpostType != Inng?ende, S? skal feilmelding gis (4) og behandling avsluttes
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
    public void shouldReturnJournalpostIdNotFoundExceptionPostVedlegg() throws IOException {
        abacPermit();
        PostLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/post_logisk_vedlegg_happy_input_request.json"), PostLogiskVedleggRequest.class);

        HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + 123443546 + "/dokumenter/" + 1234 + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseEntity.getBody(), containsString("Journalpost ikke funnet"));
    }

    @Test
    public void shouldReturnDokumentinfoIdNotFoundExceptionPostVedlegg() throws IOException {
        abacPermit();
        PostLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/post_logisk_vedlegg_happy_input_request.json"), PostLogiskVedleggRequest.class);

        Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

        String journalpostId = journalpost.getJournalpostId().toString();

        HttpEntity<PostLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + 1234 + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseEntity.getBody(), containsString("Fant ingen dokument med dokumentId=1234"));
    }

    /***************************
     ** OppdaterLogiskVedlegg **
     ***************************/

    @Test
    public void shouldUpdateLogiskVedleggUserTokenAndServiceUserToken() throws Exception {
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

        verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/putLogiskVedlegg_PersonUser_and_ServiceUser.json"),
                getBearerTokenBody(requestHttpEntity.getHeaders(), HttpHeaders.AUTHORIZATION),
                getBearerTokenBody(requestHttpEntity.getHeaders(), NAV_CONSUMER_TOKEN)))));

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
    public void shouldUpdateLogiskVedleggOnlyServiceUserToken() throws Exception {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + restStsToken(SERVICE_USER_ID));


        HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.PUT, requestHttpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), containsString("Oppdatering av logiskVedlegg med logiskVedleggId="));

        verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/putLogiskVedlegg_only_ServiceUser.json"),
                getBearerTokenBody(requestHttpEntity.getHeaders(), HttpHeaders.AUTHORIZATION)))));

        TestTransaction.start();
        Journalpost resultJournalpost = joarkRepository.findById(Long.parseLong(journalpostId)).get();
        SkannetInnhold skannetInnhold = resultJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .getSkannetInnholdListe().iterator().next();

        assertThat(skannetInnhold.getVedleggInnhold(), is("Dette er en tittel"));
        assertThat(skannetInnhold.getEndretKildeNavn(), is(SERVICE_USER_ID));
        assertThat(skannetInnhold.getChangeStamp().getUpdatedBy(), is(SERVICE_USER_ID));
        TestTransaction.end();
    }

    @Test
    public void shouldFailToUpdateLogiskVedleggOnlyPersonUserToken() throws Exception {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, openAmToken(PERSON_USER_ID));

        HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.PUT, requestHttpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
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
     * HVIS journalpostType != Inng?ende, S? skal feilmelding gis (4) og behandling avsluttes
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
    public void shouldReturnJournalpostIdNotFoundExceptionPutVedlegg() throws IOException {
        abacPermit();
        PutLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/put_logisk_vedlegg_happy_input_request.json"), PutLogiskVedleggRequest.class);

        HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + "123443546" + "/dokumenter/" + "1234" + "/logiskeVedlegg", HttpMethod.POST, requestHttpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseEntity.getBody(), containsString("Journalpost ikke funnet"));
    }

    @Test
    public void shouldReturnDokumentinfoIdNotFoundExceptionPutVedlegg() throws IOException {
        abacPermit();
        PutLogiskVedleggRequest request = mapper.readValue(classpathToString("__files/logiskvedlegg/put_logisk_vedlegg_happy_input_request.json"), PutLogiskVedleggRequest.class);

        Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

        String journalpostId = journalpost.getJournalpostId().toString();

        HttpEntity<PutLogiskVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

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
        String logiskVedleggId = "123445667067965";

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
    public void shouldDeleteLogiskVedleggUserTokenAndServiceUserToken() throws IOException {
        abacPermit();

        //Create and save testdata
        Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J).build();
        Journalpost persistedJournalpost = joarkRepository.save(journalpost);
        Long journalpostId = persistedJournalpost.getJournalpostId();
        String dokumentId = persistedJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .iterator()
                .next()
                .getDokumentInfo()
                .getDokumentInfoId()
                .toString();
        String logiskVedleggId = persistedJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .iterator()
                .next()
                .getDokumentInfo()
                .getSkannetInnholdListe()
                .iterator()
                .next()
                .getSkannetInnholdId()
                .toString();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        Journalpost updatedJournalpost = joarkRepository.findById(journalpostId).get();
        updatedJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .addSkannetInnhold(SkannetInnholdTestDataProvider.createSkannetInnhold().build());
        //Start test
        joarkRepository.save(updatedJournalpost);
        assertThat(updatedJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .getSkannetInnholdListe().size(), is(2));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpEntity<?> httpEntity = createHeaders();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.DELETE, httpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/deleteLogiskVedlegg_PersonUser_and_ServiceUser.json"),
                getBearerTokenBody(httpEntity.getHeaders(), HttpHeaders.AUTHORIZATION),
                getBearerTokenBody(httpEntity.getHeaders(), NAV_CONSUMER_TOKEN)))));

        TestTransaction.start();
        Journalpost resultJournalpost = joarkRepository.findById(journalpostId).get();
        assertThat(resultJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .getSkannetInnholdListe().size(), is(1));
        TestTransaction.end();
    }

    @Test
    public void shouldDeleteLogiskVedleggOnlyServiceUserToken() throws IOException {
        abacPermit();

        Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J).build();
        Journalpost persistedJournalpost = joarkRepository.save(journalpost);
        Long journalpostId = persistedJournalpost.getJournalpostId();
        String dokumentId = persistedJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .iterator()
                .next()
                .getDokumentInfo()
                .getDokumentInfoId()
                .toString();
        String logiskVedleggId = persistedJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .iterator()
                .next()
                .getDokumentInfo()
                .getSkannetInnholdListe()
                .iterator()
                .next()
                .getSkannetInnholdId()
                .toString();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        Journalpost updatedJournalpost = joarkRepository.findById(journalpostId).get();
        updatedJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .addSkannetInnhold(SkannetInnholdTestDataProvider.createSkannetInnhold().build());
        //Start test
        joarkRepository.save(updatedJournalpost);
        assertThat(updatedJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .getSkannetInnholdListe().size(), is(2));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + restStsToken(SERVICE_USER_ID));

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.DELETE, new HttpEntity(headers), String.class);

        verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("abac/deleteLogiskVedlegg_only_ServiceUser.json"),
                getBearerTokenBody(headers, HttpHeaders.AUTHORIZATION)))));

        TestTransaction.start();
        Journalpost resultJournalpost = joarkRepository.findById(journalpostId).get();
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(resultJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .getSkannetInnholdListe().size(), is(1));
        TestTransaction.end();
    }


    @Test
    public void shouldFailToDeleteLogiskVedleggOnlyPersonUserToken() throws IOException {
        abacPermit();

        //Create and save testdata
        Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J).build();
        Journalpost persistedJournalpost = joarkRepository.save(journalpost);
        Long journalpostId = persistedJournalpost.getJournalpostId();
        String dokumentId = persistedJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .iterator()
                .next()
                .getDokumentInfo()
                .getDokumentInfoId()
                .toString();
        String logiskVedleggId = persistedJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .iterator()
                .next()
                .getDokumentInfo()
                .getSkannetInnholdListe()
                .iterator()
                .next()
                .getSkannetInnholdId()
                .toString();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        Journalpost updatedJournalpost = joarkRepository.findById(journalpostId).get();
        updatedJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .addSkannetInnhold(SkannetInnholdTestDataProvider.createSkannetInnhold().build());
        joarkRepository.save(updatedJournalpost);
        assertThat(updatedJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .getSkannetInnholdListe().size(), is(2));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, openAmToken(PERSON_USER_ID));

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId + "/dokumenter/" + dokumentId + "/logiskeVedlegg/" + logiskVedleggId, HttpMethod.DELETE, new HttpEntity(headers), String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));

        TestTransaction.start();
        Journalpost resultJournalpost = joarkRepository.findById(journalpostId).get();
        assertThat(resultJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokumentId))
                .getSkannetInnholdListe().size(), is(2));
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
     * HVIS journalpostType != Inng?ende, S? skal feilmelding gis (4) og behandling avsluttes
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
        String logiskVedleggId = "123445667067965";

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

