package no.nav.dokarkiv.journalpost.v1.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsak;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.PutOppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.PutOppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.apache.commons.collections15.IteratorUtils;
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

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class OppdaterJournalpostIT extends AbstractOppdaterJournalpostIT {

	private static final String IDENTIFIKATOR = "***gammelt_fnr***";
	private static final String AVSENDER_MOTTAKER_NAVN = "etternavn, fornavn";
	private static final String ARKIVSAKSNUMMER = "123123";
	private static final String TEMA = "FOR";
	private static final String BEHANDLINGSTEMA = "ab0001";
	private static final String TITTEL = "Ettersendelse av something";
	private static final String AVSENDER_MOTTAKER_LAND = "Legoland";
	private static final String NOKKEL = "nokkel";
	private static final String VERDI = "verdi";
	private static final String BREVKODE = "brevkode";

	/**
	 * HVIS forsoekEndeligJF == TRUE, og ingen felter mangler for å endelig journalføre => returner 200 OK og journalpostId.
	 */
	@Test
	public void shouldFerdigstillJournalpostVedOppdateringUserTokenAndServiceUserToken() throws IOException {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();

		PutOppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpEntity<PutOppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<PutOppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutOppdaterJournalpostResponse.class);

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
		assertThat(oppdatertJP.getLand(), is(request.getAvsenderMottakerLand()));
		assertThat(oppdatertJP.getAvsenderMottakerId(), is(request.getAvsenderMottaker().getIdentifikator()));
		assertThat(oppdatertJP.getAvsenderMottaker(), is(request.getAvsenderMottaker().getAvsenderMottakerNavn()));
		assertThat(oppdatertJP.getBrukere().size(), is(1));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId(), is(request.getBruker().getIdentifikator()));
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType(), is(BrukerTypeCode.PERSON));
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertThat(oppdatertJP.getSaksrelasjon().getSakId(), is(request.getArkivsak().getArkivsaksnummer()));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name(), is("FS22"));
		assertThat(oppdatertJP.getTilleggsopplysninger().size(), is(1));
		assert(oppdatertJP.getTilleggsopplysninger().containsKey(request.getTilleggsopplysninger().get(0).getNokkel()));
		assert(oppdatertJP.getTilleggsopplysninger().containsValue(request.getTilleggsopplysninger().get(0).getVerdi()));
		assertThat(oppdatertJP.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(request.getDokumentInfoList().get(0).getDokumentInfoId())).getTittel(),
				is(request.getDokumentInfoList().get(0).getTittel()));
		assertThat(oppdatertJP.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(request.getDokumentInfoList().get(0).getDokumentInfoId())).getBrevkode(),
				is(request.getDokumentInfoList().get(0).getBrevkode()));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());

		assertEquals(3, aksjonsLoggList.size());

		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(AksjonsTypeCode.SAKSTILKNYTNING, aksjonsLoggList.get(0).getAksjon());
		assertEquals(2, aksjonsLoggList.get(0).getArkivElementEndringer().size());

		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(1).getUtfoertAv());
		assertEquals(AksjonsTypeCode.ENDRE_METADATA, aksjonsLoggList.get(1).getAksjon());
		assertEquals(2, aksjonsLoggList.get(1).getArkivElementEndringer().size());

		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(2).getUtfoertAv());
		assertEquals(AksjonsTypeCode.ENDRE_METADATA, aksjonsLoggList.get(2).getAksjon());
		assertEquals(1, aksjonsLoggList.get(2).getArkivElementEndringer().size());

		TestTransaction.end();
	}

	@Test
	public void shouldNotProduceAksjonsLogg() {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		PutOppdaterJournalpostRequest request = new PutOppdaterJournalpostRequest();

		HttpEntity<PutOppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<PutOppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutOppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.start();
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assert(aksjonsLoggList.isEmpty());
		TestTransaction.end();

	}

	@Test
	public void shouldFerdigstillJournalpostVedOppdateringOnlyServiceUserToken() throws IOException {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();

		PutOppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);

		HttpEntity<PutOppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<PutOppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutOppdaterJournalpostResponse.class);

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
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();

		PutOppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);

		HttpEntity<PutOppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<PutOppdaterJournalpostResponse> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, PutOppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

	@Test
	public void shouldReturnForbiddenBrukerHarIkkeTilgangTilJournalpostPutJournalpost() throws IOException {
		abacDeny();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();

		PutOppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpEntity<PutOppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER + journalpostId, HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		assertThat(responseEntity.getBody(), containsString("Bruker har ikke tilgang til journalpost"));
	}

	private PutOppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithDokumentInfoId(Long dokumentInfoId) {
		return PutOppdaterJournalpostRequest.builder()
				.avsenderMottaker(AvsenderMottaker.builder()
						.identifikator(IDENTIFIKATOR)
						.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN)
						.build())
				.bruker(Bruker.builder()
						.brukerIdType(BrukerIdType.FNR)
						.identifikator(IDENTIFIKATOR)
						.build())
				.arkivsak(Arkivsak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.build())
				.tema(TEMA)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(TITTEL)
				.avsenderMottakerLand(AVSENDER_MOTTAKER_LAND)
				.tilleggsopplysninger(Arrays.asList(Tilleggsopplysning.builder()
						.nokkel(NOKKEL)
						.verdi(VERDI)
						.build()))
				.dokumentInfoList(Arrays.asList(DokumentInfo.builder()
						.dokumentInfoId(Long.toString(dokumentInfoId))
						.brevkode(BREVKODE)
						.tittel(TITTEL)
						.build()))
				.build();
	}
}


