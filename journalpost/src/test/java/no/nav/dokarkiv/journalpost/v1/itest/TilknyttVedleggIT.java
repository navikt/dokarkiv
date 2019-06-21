package no.nav.dokarkiv.journalpost.v1.itest;

import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.createDokumentFil;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.createDokumentFilSladdet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.ArsakFeilCode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggIT extends AbstractJournalpostIT{

	private static final String UGYLDIG_JOURNALPOST = "***gammelt_fnr***";


	@Test
	public void shouldTilknytteVedleggTilJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpostVedlegg = createJournalpost();
		Journalpost journalpostOrg = createOrgJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long journalpostIdOrg = joarkRepository.save(journalpostOrg).getJournalpostId();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		Long dokumentInfoId = journalpostOrg.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostIdOrg)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		Journalpost journalpostTilknyttetVedlegg = joarkRepository.findById(journalpostIdVedlegg).get();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(journalpostTilknyttetVedlegg.findAllDokumentInfos().get(0).getTilleggsopplysninger().get("Kopi dokumentInfoId"),
				is(request.getDokument().get(0).getDokumentInfoId()));
	}

	@Test
	public void shouldReturnForbiddenForWrongConsumer() throws IOException {
		abacPermit();

		Journalpost journalpostVedlegg = createJournalpost();
		Journalpost journalpostOrg = createOrgJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long journalpostIdOrg = joarkRepository.save(journalpostOrg).getJournalpostId();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		Long dokumentInfoId = journalpostOrg.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostIdOrg)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkiv" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
	}

	@Test
	public void shouldReturnNotFoundForJournalpost() throws IOException {
		abacPermit();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + UGYLDIG_JOURNALPOST + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	@Test
	public void shouldReturnConflictForJournalpostWrongStatus() throws IOException {
		abacPermit();

		Journalpost journalpostVedlegg = createJournalpost();
		journalpostVedlegg.setJournalstatus(JournalStatusCode.M);
		Journalpost journalpostOrg = createOrgJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long journalpostIdOrg = joarkRepository.save(journalpostOrg).getJournalpostId();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		Long dokumentInfoId = journalpostOrg.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostIdOrg)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.CONFLICT));

	}

	@Test
	public void shouldReturnFeiletDokumentListeAarsakKodeUgyldigStatus() throws IOException {
		abacPermit();

		Journalpost journalpostVedlegg = createJournalpost();
		Journalpost journalpostOrg = createOrgJournalpostSladdet();
		journalpostOrg.setJournalstatus(JournalStatusCode.M);
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long journalpostIdOrg = joarkRepository.save(journalpostOrg).getJournalpostId();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		Long dokumentInfoId = journalpostOrg.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostIdOrg)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiletDokument().get(0).getArsakKode(), is(ArsakFeilCode.UGYLDIG_STATUS) );

	}

	@Test
	public void shouldReturnFeiletDokumentListeAarsakKodeIkkeFunnet() throws IOException {
		abacPermit();

		Journalpost journalpostVedlegg = createJournalpost();
		Journalpost journalpostOrg = createOrgJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long journalpostIdOrg = joarkRepository.save(journalpostOrg).getJournalpostId();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostIdOrg)
				.dokumentInfoId("200000345")
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiletDokument().get(0).getArsakKode(), is(ArsakFeilCode.IKKE_FUNNET) );

	}



	private TilknyttVedleggRequest createTilknyttVedleggRequest(List<DokumentVedlegg> dokumentVedleggList){
		return TilknyttVedleggRequest.builder()
				.tilknyttetAvNavn("TilknyttVedleggIT")
				.dokument(dokumentVedleggList)
				.build();
	}
	private Journalpost createJournalpost() {
		return JournalpostTestDataProvider.createJournalpostWithoutHoveddokument()
				.journalStatus(JournalStatusCode.D)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("opprettetAvNavn")
				.opprettetKildeNavn("opprettetKildeNavn")
				.endretKildeNavn("endretKildeNavn")
				.endretAvNavn("endretAvNavn")
				.build();
	}
	private Journalpost createOrgJournalpostSladdet() {
		dokumentFilRepository.save(createDokumentFil().build());
		dokumentFilRepository.save(createDokumentFilSladdet().build());
		return JournalpostTestDataProvider.createJournalpost(DokumentFilTestDataProvider.FIL_UUID, DokumentFilTestDataProvider.FIL_UUID_SLADDET)
				.journalStatus(JournalStatusCode.J)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("opprettetAvNavn")
				.opprettetKildeNavn("opprettetKildeNavn")
				.endretKildeNavn("endretKildeNavn")
				.endretAvNavn("endretAvNavn")
				.build();
	}
}
