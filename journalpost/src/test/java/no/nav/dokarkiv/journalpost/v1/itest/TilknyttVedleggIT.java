package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.stream.Collectors.joining;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.FAR;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.KTA;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.PEN;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataUtils.KANAL_REFERANSE_ID;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.IKKE_FUNNET;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.UGYLDIG_STATUS;
import static no.nav.dokarkiv.journalpost.v1.util.FunctionalMatcher.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.MULTI_STATUS;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class TilknyttVedleggIT extends AbstractJournalpostIT {

	private static final String TILKNYTT_VEDLEGG_PATH = "/tilknyttVedlegg";
	private static final String UGYLDIG_JOURNALPOST = "12312312312";
	private static final String TILLEGGOPPLYSNINGER_KEY = "DOK_ORG_DOK_INFO_ID";

	@BeforeEach
	public void setUp() {
		super.setUp();
		stubMsGraphGetUser(NAV_USER_ID);
		stubAzure();
	}

	@ParameterizedTest
	@EnumSource(value = JournalpostTypeCode.class, names = {"U", "N"})
	public void shouldTilknytteArkivVedleggTilJournalpost(JournalpostTypeCode journalpostTypeCode) {
		Journalpost targetJournalpost = createJournalpostArkiv(journalpostTypeCode);
		Journalpost sourceJournalpost = createJournalpostArkiv(journalpostTypeCode);
		sourceJournalpost.setJournalstatus(J);
		Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
		Long sourcejournalpostId = saveJournalpost(sourceJournalpost).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost);
		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourcejournalpostId)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		var responseEntity = restTemplate.exchange(
				apiJournalpostPath(targetJournalpostId + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, String.class);

		completeCurrentAndStartNewTransaction();

		Journalpost journalpostTilknyttetVedlegg = journalpostTestRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		assertThat(responseEntity.getStatusCode(), is(OK));

		Optional<DokumentInfo> dokumentInfoKopi = journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
				.filter(j -> j.getDokumentInfoId().equals(dokumentInfoId)).findAny();
		assertTrue(dokumentInfoKopi.isPresent());
		assertEquals(sourceDokumentInfo.getDokumentInfoId(), dokumentInfoKopi.get().getDokumentInfoId());
	}

	@Test
	public void shouldTilknytteFlereVedleggTilJournalpost() {
		Journalpost targetJournalpost = createJournalpostArkiv(U);
		Journalpost sourceJournalpost1 = createJournalpostSladdet();
		Journalpost sourceJournalpost2 = createJournalpostSladdet();
		Journalpost sourceJournalpost3 = createJournalpostArkiv(U);
		sourceJournalpost3.setJournalstatus(J);
		Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
		Long sourceJournalpostId1 = saveJournalpost(sourceJournalpost1).getJournalpostId();
		Long sourceJournalpostId2 = saveJournalpost(sourceJournalpost2).getJournalpostId();
		Long sourceJournalpostId3 = saveJournalpost(sourceJournalpost3).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost1, sourceJournalpost2, sourceJournalpost3);
		completeCurrentAndStartNewTransaction();

		Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId1, sourceDokumentInfoId1.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId2, sourceDokumentInfoId2.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId3, sourceDokumentInfoId3.toString()));

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				apiJournalpostPath(targetJournalpostId + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		completeCurrentAndStartNewTransaction();

		assertThat(responseEntity.getStatusCode(), is(OK));

		//Assert 1 Sladdet
		Journalpost journalpostTilknyttetVedlegg1 = journalpostTestRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi1 = journalpostTilknyttetVedlegg1.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId1.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer1 = sourceDokumentInfo1.findFilDetaljerByVariantFormat(SLADDET);
		FilDetaljer filDetaljerKopi1 = dokumentInfoKopi1.findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil sourceDokumentFil1 = dokumentFilTestRepository.findByFilUuid(sourceFilDetaljer1.getFilUuid());
		DokumentFil dokumentFilKopi1 = dokumentFilTestRepository.findByFilUuid(filDetaljerKopi1.getFilUuid());

		assertRelasjon(journalpostTilknyttetVedlegg1.getJournalpostId(), dokumentInfoKopi1);
		assertDokumentInfo(sourceDokumentInfo1, dokumentInfoKopi1);
		assertFildetaljer(sourceFilDetaljer1, filDetaljerKopi1);
		assertDokumentFil(sourceDokumentFil1, dokumentFilKopi1);

		//Assert 2 sladdet
		Journalpost journalpostTilknyttetVedlegg2 = journalpostTestRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo2 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi2 = journalpostTilknyttetVedlegg2.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId2.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer2 = sourceDokumentInfo2.findFilDetaljerByVariantFormat(SLADDET);
		FilDetaljer filDetaljerKopi2 = dokumentInfoKopi2.findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil sourceDokumentFil2 = dokumentFilTestRepository.findByFilUuid(sourceFilDetaljer2.getFilUuid());
		DokumentFil dokumentFilKopi2 = dokumentFilTestRepository.findByFilUuid(filDetaljerKopi2.getFilUuid());

		assertRelasjon(journalpostTilknyttetVedlegg2.getJournalpostId(), dokumentInfoKopi2);
		assertDokumentInfo(sourceDokumentInfo2, dokumentInfoKopi2);
		assertFildetaljer(sourceFilDetaljer2, filDetaljerKopi2);
		assertDokumentFil(sourceDokumentFil2, dokumentFilKopi2);


		//Assert 3 Arkiv
		Journalpost journalpostTilknyttetVedlegg = journalpostTestRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi3 = journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getDokumentInfoId().equals(sourceDokumentInfoId3))
				.findAny()
				.get()
				.getDokumentInfo();

		assertThat(responseEntity.getStatusCode(), is(OK));
		assertEquals(sourceDokumentInfo3.getDokumentInfoId(), dokumentInfoKopi3.getDokumentInfoId());
	}

	@Test
	public void shouldTilknytte2av3VedleggTilJournalpost() {
		Journalpost journalpostVedlegg = createJournalpostArkiv(U);
		Journalpost sourceJournalpost1 = createJournalpostSladdet();
		Journalpost sourceJournalpost2 = createJournalpostSladdet();
		Journalpost sourceJournalpost3 = createJournalpostArkiv(U);
		Long journalpostIdVedlegg = saveJournalpost(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId1 = saveJournalpost(sourceJournalpost1).getJournalpostId();
		Long sourceJournalpostId2 = saveJournalpost(sourceJournalpost2).getJournalpostId();
		Long sourceJournalpostId3 = saveJournalpost(sourceJournalpost3).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost1, sourceJournalpost2, sourceJournalpost3);
		completeCurrentAndStartNewTransaction();

		Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId1, sourceDokumentInfoId1.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId2, sourceDokumentInfoId2.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId3, sourceDokumentInfoId3.toString()));

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		completeCurrentAndStartNewTransaction();

		assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(UGYLDIG_STATUS));

		//Assert 1 Sladdet
		Journalpost journalpostTilknyttetVedlegg1 = journalpostTestRepository.findById(journalpostIdVedlegg).get();
		DokumentInfo sourceDokumentInfo1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi1 = journalpostTilknyttetVedlegg1.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId1.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer1 = sourceDokumentInfo1.findFilDetaljerByVariantFormat(SLADDET);
		FilDetaljer filDetaljerKopi1 = dokumentInfoKopi1.findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil sourceDokumentFil1 = dokumentFilTestRepository.findByFilUuid(sourceFilDetaljer1.getFilUuid());
		DokumentFil dokumentFilKopi1 = dokumentFilTestRepository.findByFilUuid(filDetaljerKopi1.getFilUuid());

		assertRelasjon(journalpostTilknyttetVedlegg1.getJournalpostId(), dokumentInfoKopi1);
		assertDokumentInfo(sourceDokumentInfo1, dokumentInfoKopi1);
		assertFildetaljer(sourceFilDetaljer1, filDetaljerKopi1);
		assertDokumentFil(sourceDokumentFil1, dokumentFilKopi1);

		//Assert 2 sladdet
		Journalpost journalpostTilknyttetVedlegg2 = journalpostTestRepository.findById(journalpostIdVedlegg).get();
		DokumentInfo sourceDokumentInfo2 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi2 = journalpostTilknyttetVedlegg2.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId2.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer2 = sourceDokumentInfo2.findFilDetaljerByVariantFormat(SLADDET);
		FilDetaljer filDetaljerKopi2 = dokumentInfoKopi2.findFilDetaljerByVariantFormat(ARKIV);
		DokumentFil sourceDokumentFil2 = dokumentFilTestRepository.findByFilUuid(sourceFilDetaljer2.getFilUuid());
		DokumentFil dokumentFilKopi2 = dokumentFilTestRepository.findByFilUuid(filDetaljerKopi2.getFilUuid());

		assertRelasjon(journalpostTilknyttetVedlegg2.getJournalpostId(), dokumentInfoKopi2);
		assertDokumentInfo(sourceDokumentInfo2, dokumentInfoKopi2);
		assertFildetaljer(sourceFilDetaljer2, filDetaljerKopi2);
		assertDokumentFil(sourceDokumentFil2, dokumentFilKopi2);

		//Assert 3 Arkiv
		Journalpost journalpostTilknyttetVedlegg = journalpostTestRepository.findById(journalpostIdVedlegg).get();
		assertThat(journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.anyMatch(j -> j.getDokumentInfo().getDokumentInfoId().equals(sourceDokumentInfoId3)), is(false));
	}

	@Test
	public void shouldOnlyPerformAccessLookupForFagomraadeFARAndKTA() {
		Journalpost journalpostVedlegg = createJournalpostArkiv(U);
		Journalpost sourceJournalpost1 = createJournalpostArkiv(U);
		Journalpost sourceJournalpost2 = createJournalpostArkiv(U);
		Journalpost sourceJournalpost3 = createJournalpostArkiv(U);

		sourceJournalpost1.setFagomrade(PEN);
		sourceJournalpost2.setFagomrade(FAR);
		sourceJournalpost3.setFagomrade(KTA);

		Long journalpostIdVedlegg = saveJournalpost(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId1 = saveJournalpost(sourceJournalpost1).getJournalpostId();
		Long sourceJournalpostId2 = saveJournalpost(sourceJournalpost2).getJournalpostId();
		Long sourceJournalpostId3 = saveJournalpost(sourceJournalpost3).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost1, sourceJournalpost2, sourceJournalpost3);
		completeCurrentAndStartNewTransaction();

		Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId1, sourceDokumentInfoId1.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId2, sourceDokumentInfoId2.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId3, sourceDokumentInfoId3.toString()));

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		restTemplate.exchange(apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		completeCurrentAndStartNewTransaction();

		verify(2, postRequestedFor(urlEqualTo("/safgraphql")));
	}

	@Test
	public void shouldReturnForbiddenForWrongConsumer() {
		Journalpost journalpostVedlegg = createJournalpostArkiv(U);
		Journalpost sourceJournalpost = createJournalpostSladdet();
		Long journalpostIdVedlegg = journalpostTestRepository.persist(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost);
		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
				.toString()));

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		var responseEntity = restTemplate.exchange(
				apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, String.class);
		assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
	}

	@Test
	public void shouldReturnNotFoundForJournalpost() {
		generateAndStubSafResponse();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				apiJournalpostPath(UGYLDIG_JOURNALPOST + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	@Test
	public void shouldReturnConflictForJournalpostWrongStatus() {
		Journalpost sourceJournalpost = createJournalpostSladdet();
		sourceJournalpost.setJournalstatus(M);
		Long journalpostIdVedlegg = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();
		Long sourceJournalpostId = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost);
		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
				.toString()));

		var requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(CONFLICT));
	}

	@Test
	public void shouldReturnFeiletDokumentListeAarsakKodeUgyldigStatus() {
		Journalpost journalpostVedlegg = createJournalpostArkiv(U);
		Journalpost sourceJournalpost = createJournalpostSladdet();
		sourceJournalpost.setJournalstatus(M);
		Long journalpostIdVedlegg = journalpostTestRepository.persist(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost);
		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
				.toString()));

		var requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);


		assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiledeDokumenter(), hasItem(allOf(
				where(dok -> Long.parseLong(((FeiledeDokumenter) dok).getDokumentInfoId()), is(dokumentInfoId)),
				where(FeiledeDokumenter::getArsakKode, is(UGYLDIG_STATUS))
		)));
	}

	@Test
	public void shouldReturnFeiletDokumentListeAarsakKodeIkkeFunnet() {
		Journalpost journalpostVedlegg = createJournalpostArkiv(U);
		Journalpost sourceJournalpost = createJournalpostSladdet();
		Long journalpostIdVedlegg = journalpostTestRepository.persist(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost);
		completeCurrentAndStartNewTransaction();

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, "200000345"));

		var requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(IKKE_FUNNET));
	}

	@Test
	void shouldReturnFeiletDokumentListeArsakKodeIkkeFunnetWhenDokumentFilDoesNotExist() {
		Journalpost targetJournalpost = createJournalpostArkiv(U);
		Journalpost sourceJournalpost = createJournalpostArkiv(U);
		sourceJournalpost.setJournalstatus(J);
		Journalpost sourceJournalpostSladdet = createJournalpostSladdet();
		sourceJournalpostSladdet.setJournalstatus(J);
		Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
		Long sourcejournalpostId = saveJournalpost(sourceJournalpost).getJournalpostId();
		Long sourceJournalpostIdSladdet = saveJournalpost(sourceJournalpostSladdet).getJournalpostId();

		generateAndStubSafResponse(sourceJournalpost, sourceJournalpostSladdet);
		completeCurrentAndStartNewTransaction();

		JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon();
		JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjonSladdet = sourceJournalpostSladdet.findHoveddokumentDokumentInfoRelasjon();
		dokumentFilTestRepository.deleteByFilUuid(hoveddokumentDokumentInfoRelasjon.getDokumentInfo().getFildetaljerListe().stream().findFirst().get().getFilUuid());
		dokumentFilTestRepository.deleteByFilUuid(hoveddokumentDokumentInfoRelasjonSladdet.getDokumentInfo().getFildetaljerListe().stream().filter(f -> SLADDET == f.getVariantFormat()).findFirst().get().getFilUuid());
		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = hoveddokumentDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId();
		Long dokumentInfoIdSladdet = hoveddokumentDokumentInfoRelasjonSladdet.getDokumentInfo().getDokumentInfoId();
		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourcejournalpostId)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostIdSladdet)
				.dokumentInfoId(dokumentInfoIdSladdet.toString())
				.build());

		HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		var responseEntity = restTemplate.exchange(
				apiJournalpostPath(targetJournalpostId + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiledeDokumenter(), hasSize(2));
		assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(IKKE_FUNNET));
		assertThat(responseEntity.getBody().getFeiledeDokumenter().get(1).getArsakKode(), is(IKKE_FUNNET));
	}

	private void assertRelasjon(Long journalpostIdTilknyttet, DokumentInfo dokumentInfoKopi) {
		JournalpostDokumentInfoRelasjon tilknyttetRelasjon = dokumentInfoKopi.findJournalpostRelasjonByJournalpostId(journalpostIdTilknyttet);
		assertThat(tilknyttetRelasjon.getTilknyttetAvNavn(), is(PERSON_USER_NAME));
		assertThat(tilknyttetRelasjon.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
	}

	private void assertDokumentInfo(DokumentInfo sourceDokumentInfo, DokumentInfo dokumentInfoKopi) {
		assertEquals(sourceDokumentInfo.getDokumentstatus(), dokumentInfoKopi.getDokumentstatus());
		assertThat(sourceDokumentInfo.getDokumentFerdigDato()).isCloseTo(dokumentInfoKopi.getDokumentFerdigDato(), 200);
		assertEquals(sourceDokumentInfo.getTittel(), dokumentInfoKopi.getTittel());
		assertEquals(sourceDokumentInfo.getBrevkode(), dokumentInfoKopi.getBrevkode());
		assertEquals(sourceDokumentInfo.getDokumenttypeId(), dokumentInfoKopi.getDokumenttypeId());
		assertEquals(sourceDokumentInfo.getBrevgruppe(), dokumentInfoKopi.getBrevgruppe());
		assertNull(dokumentInfoKopi.getOriginalJournalpost());
		assertEquals(sourceDokumentInfo.getSensitivt(), dokumentInfoKopi.getSensitivt());
		assertEquals(sourceDokumentInfo.getKonvertertFraSystem(), dokumentInfoKopi.getKonvertertFraSystem());
		assertNull(dokumentInfoKopi.getEndretAvNavn());
		assertEquals(sourceDokumentInfo.getKassertAvNavn(), dokumentInfoKopi.getKassertAvNavn());
		assertEquals(sourceDokumentInfo.getDatoKassert(), dokumentInfoKopi.getDatoKassert());
		assertThat(dokumentInfoKopi.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertNull(dokumentInfoKopi.getEndretKildeNavn());
	}

	private void assertFildetaljer(FilDetaljer sourceFilDetaljer, FilDetaljer filDetaljerKopi) {
		assertEquals(sourceFilDetaljer.getFiltype(), filDetaljerKopi.getFiltype());
		assertEquals(sourceFilDetaljer.getOnDemandId(), filDetaljerKopi.getOnDemandId());
		assertEquals(sourceFilDetaljer.getOnDemandInstans(), filDetaljerKopi.getOnDemandInstans());
		assertEquals(sourceFilDetaljer.getMetaforceInstanceId(), filDetaljerKopi.getMetaforceInstanceId());
		assertThat(filDetaljerKopi.getVariantFormat(), is(ARKIV));
		assertThat(filDetaljerKopi.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
		assertEquals(sourceFilDetaljer.getBatchNavn(), filDetaljerKopi.getBatchNavn());
		assertEquals(sourceFilDetaljer.getFilnavn(), filDetaljerKopi.getFilnavn());
		assertEquals(sourceFilDetaljer.getFilstorrelse(), filDetaljerKopi.getFilstorrelse());
		assertEquals(sourceFilDetaljer.getSkjermingType(), filDetaljerKopi.getSkjermingType());
		assertNull(filDetaljerKopi.getEndretKildeNavn());
	}

	private void assertDokumentFil(DokumentFil sourceDokumentFil, DokumentFil dokumentFilKopi) {
		assertEquals(new String(sourceDokumentFil.getFil()), new String(dokumentFilKopi.getFil()));
		assertThat(dokumentFilKopi.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
	}

	private TilknyttVedleggRequest createTilknyttVedleggRequest(List<DokumentVedlegg> dokumentVedleggList) {
		return TilknyttVedleggRequest.builder()
				.tilknyttetAvNavn("TilknyttVedleggIT")
				.dokument(dokumentVedleggList)
				.build();
	}

	private Journalpost createJournalpostSladdet() {
		Journalpost journalpostSladdet = createJournalpostWithHoveddokument();
		journalpostSladdet.setJournalstatus(J);
		journalpostSladdet.setJournalposttype(U);
		journalpostSladdet.setOpprettetAvNavn("opprettetAvNavn");
		journalpostSladdet.setOpprettetKildeNavn("opprettetKildeNavn");
		journalpostSladdet.setEndretKildeNavn("endretKildeNavn");
		journalpostSladdet.setEndretAvNavn("endretAvNavn");
		journalpostSladdet.setKanalReferanseId(KANAL_REFERANSE_ID + UUID.randomUUID());

		DokumentInfo dokumentInfo = journalpostSladdet.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.removeFilDetaljer(dokumentInfo.findFilDetaljerByVariantFormat(PRODUKSJON));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, SLADDET));
		return journalpostSladdet;
	}

	private Journalpost createJournalpostArkiv(JournalpostTypeCode journalpostTypeCode) {
		Journalpost journalpostArkiv = createJournalpostWithHoveddokument();
		journalpostArkiv.setJournalstatus(D);
		journalpostArkiv.setJournalposttype(journalpostTypeCode);
		journalpostArkiv.setOpprettetAvNavn("opprettetAvNavn");
		journalpostArkiv.setOpprettetKildeNavn("opprettetKildeNavn");
		journalpostArkiv.setEndretKildeNavn("endretKildeNavn");
		journalpostArkiv.setEndretAvNavn("endretAvNavn");
		journalpostArkiv.setKanalReferanseId(KANAL_REFERANSE_ID + UUID.randomUUID());

		DokumentInfo dokumentInfo = journalpostArkiv.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.removeFilDetaljer(dokumentInfo.findFilDetaljerByVariantFormat(PRODUKSJON));
		return journalpostArkiv;
	}

	private List<DokumentVedlegg> createDokumentVedleggList(Long journalpostId, String dokumentinfoId) {
		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(createDokumentVedlegg(journalpostId, dokumentinfoId));
		return dokumentVedleggList;
	}

	private DokumentVedlegg createDokumentVedlegg(Long journalpostId, String dokumentinfoId) {
		return DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostId)
				.dokumentInfoId(dokumentinfoId)
				.build();
	}

	private void completeCurrentAndStartNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	private static void generateAndStubSafResponse(Journalpost... journalposts) {
		String response = """
				{
				"data": {
				  "journalpost": {
				    "dokumenter": [
				    """ +
				Stream.of(journalposts)
						.map(Journalpost::findHoveddokumentDokumentInfoRelasjon)
						.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
						.map(DokumentInfo::getDokumentInfoId)
						.map(id -> String.format("""
								  {
								  "dokumentInfoId": "%d",
								  "dokumentvarianter": [
									{
									  "saksbehandlerHarTilgang": true,
									  "variantformat": "ARKIV"
									}
								  ]
								}""", id))
						.collect(joining(","))
				+ "] }}}";

		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(response)));
	}

}
